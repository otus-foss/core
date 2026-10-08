"""Regression tests showing representative legal/illegal library dependency directions."""

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_architecture import violations


def check(module, package, body, source_set="commonMain"):
    base = "com.otus." + module.replace("-", ".")
    name = base + (f".{package}" if package else "")
    path = Path(f"{module}/src/{source_set}/kotlin") / name.replace(".", "/") / "Example.kt"
    return violations(path, f"package {name}\n{body}", frozenset({"designsystem"}))


class ProtocolModuleTests(unittest.TestCase):
    def test_networking_is_allowed(self):
        self.assertEqual(check("jmap", "", "import io.ktor.client.HttpClient"), [])

    def test_hyphenated_module_maps_to_dotted_namespace(self):
        self.assertEqual(check("jmap-mail", "query", "import io.ktor.client.HttpClient"), [])

    def test_compose_is_rejected(self):
        self.assertTrue(check("jmap", "", "import androidx.compose.runtime.Composable"))
        self.assertTrue(check("contacts", "domain", "@Composable fun Card() {}"))

    def test_app_code_is_rejected_in_every_source_set(self):
        self.assertTrue(check("jmap", "login", "import com.otus.mail.app.App"))
        self.assertTrue(check("jmap", "", "fun x() = com.otus.calendar.app.App()"))
        self.assertTrue(check("jmap", "", "import com.otus.mail.app.App", "commonTest"))

    def test_common_code_cannot_use_platform_apis(self):
        self.assertTrue(check("jmap", "", "import android.content.Context"))
        self.assertTrue(check("jmap", "", "fun now() = platform.Foundation.NSDate()"))
        self.assertEqual(
            check("jmap", "login", "import android.content.Context", "androidMain"), []
        )

    def test_domain_cannot_use_network_or_database(self):
        self.assertTrue(check("contacts", "domain", "import io.ktor.client.HttpClient"))
        self.assertTrue(check("contacts", "domain", "import app.cash.sqldelight.Query"))

    def test_package_must_match_module_namespace(self):
        path = Path("jmap/src/commonMain/kotlin/com/otus/jmap/Example.kt")
        self.assertTrue(violations(path, "package com.otus.mail.core\n"))
        path = Path("jmap/src/commonMain/kotlin/com/otus/contacts/Example.kt")
        self.assertTrue(violations(path, "package com.otus.contacts\n"))

    def test_catch_all_packages_are_rejected(self):
        self.assertTrue(check("jmap", "utils", "fun x() = 1"))

    def test_wildcard_imports_and_suppressions_are_rejected(self):
        self.assertTrue(check("jmap", "", "import io.ktor.client.*"))
        self.assertTrue(check("jmap", "", '@Suppress("unused") fun x() = 1'))


class DesignSystemTests(unittest.TestCase):
    def test_compose_is_allowed_in_layers(self):
        body = "import androidx.compose.runtime.Composable\n@Composable fun Chip() {}"
        self.assertEqual(check("designsystem", "elements", body), [])

    def test_code_outside_layers_is_rejected(self):
        self.assertTrue(check("designsystem", "widgets", "fun x() = 1"))
        self.assertTrue(check("designsystem", "", "fun x() = 1"))

    def test_elements_cannot_import_components(self):
        body = "import com.otus.designsystem.components.Sidebar"
        self.assertTrue(check("designsystem", "elements", body))
        self.assertEqual(check("designsystem", "components", body), [])

    def test_cannot_depend_on_protocol_or_data_modules(self):
        self.assertTrue(check("designsystem", "elements", "import com.otus.jmap.JmapClient"))
        self.assertTrue(check("designsystem", "components", "import io.ktor.client.HttpClient"))

    def test_components_take_values_not_services(self):
        self.assertTrue(check("designsystem", "components", "fun row(vm: InboxViewModel) {}"))

    def test_literal_colors_belong_in_theme(self):
        self.assertTrue(check("designsystem", "elements", "val c = Color(0xFFFFFFFF)"))
        self.assertEqual(check("designsystem", "theme", "val c = Color(0xFFFFFFFF)"), [])

    def test_portable_compose_platform_package_is_allowed(self):
        body = "import androidx.compose.ui.platform.LocalDensity"
        self.assertEqual(check("designsystem", "elements", body), [])


if __name__ == "__main__":
    unittest.main()
