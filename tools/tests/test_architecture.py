"""Regression tests showing representative legal/illegal library dependency directions."""

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_architecture import source_code, violations, visual_resource_violations


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

    def test_compose_is_rejected_outside_presentation(self):
        self.assertTrue(check("jmap", "", "import androidx.compose.runtime.Composable"))
        self.assertTrue(check("contacts", "domain", "@Composable fun Card() {}"))
        self.assertTrue(check("accounts", "data", "import androidx.compose.runtime.Composable"))

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

    def test_qualified_and_android_suppressions_are_rejected(self):
        for annotation in (
            '@SuppressLint("NewApi") fun x() = 1',
            '@android.annotation.SuppressLint("NewApi") fun x() = 1',
            '@kotlin.Suppress("unused") fun x() = 1',
            '@SuppressWarnings("unused") fun x() = 1',
        ):
            with self.subTest(annotation=annotation):
                self.assertTrue(check("jmap", "", annotation, "androidMain"))
        path = Path("jmap/src/commonMain/kotlin/com/otus/jmap/Example.kt")
        for directive in ('@file:kotlin.Suppress("unused")', '@file: Suppress("unused")'):
            with self.subTest(directive=directive):
                self.assertTrue(violations(path, f"{directive}\npackage com.otus.jmap\n"))

    def test_string_templates_are_still_checked(self):
        body = 'fun x() = "${android.os.Build.MODEL} and ${com.otus.mail.app.App()}"'
        errors = check("jmap", "", body)
        self.assertIn("platform-only API in commonMain", errors)
        self.assertIn("shared libraries cannot depend on Mail or Calendar app code", errors)
        self.assertEqual(check("jmap", "", 'val x = "android.os.Build and $name"'), [])

    def test_source_code_keeps_template_code_and_line_count(self):
        text = 'val a = "x ${f("}")} y"\nval b = """\n${g { 1 }}\n"""\nval c = \'"\' // "\n'
        code = source_code(text)
        self.assertEqual(text.count("\n"), code.count("\n"))
        self.assertIn("f(", code)
        self.assertIn("g { 1 }", code)
        self.assertNotIn("x ", code)
        self.assertIn("val c", code)


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

    def test_cannot_depend_on_protocol_or_feature_modules(self):
        self.assertTrue(check("designsystem", "elements", "import com.otus.jmap.JmapClient"))
        self.assertTrue(check("designsystem", "components", "import io.ktor.client.HttpClient"))
        body = "import com.otus.accounts.domain.ConnectedAccount"
        self.assertTrue(check("designsystem", "components", body))

    def test_may_format_dates(self):
        body = "import com.otus.dateformat.DateFormatter"
        self.assertEqual(check("designsystem", "components", body), [])
        self.assertTrue(check("designsystem", "components", "import com.otus.dateformatting.Store"))

    def test_components_take_values_not_services(self):
        self.assertTrue(check("designsystem", "components", "fun row(vm: InboxViewModel) {}"))

    def test_literal_colors_belong_in_theme(self):
        self.assertTrue(check("designsystem", "elements", "val c = Color(0xFFFFFFFF)"))
        self.assertEqual(check("designsystem", "theme", "val c = Color(0xFFFFFFFF)"), [])

    def test_aliased_and_decimal_literal_colors_belong_in_theme(self):
        aliased = "import androidx.compose.ui.graphics.Color as Tint\nval c = Tint(0xFFFFFFFF)"
        self.assertTrue(check("designsystem", "elements", aliased))
        self.assertTrue(check("designsystem", "elements", "val c = Color(red = 0.5f, 0f, 0f)"))
        self.assertTrue(check("designsystem", "elements", "val c = Color(255, 0, 0)"))
        token = "val c = Color(OtusColors.text1.value)"
        self.assertEqual(check("designsystem", "elements", token), [])

    def test_portable_compose_platform_package_is_allowed(self):
        body = "import androidx.compose.ui.platform.LocalDensity"
        self.assertEqual(check("designsystem", "elements", body), [])


class FeatureModuleTests(unittest.TestCase):
    compose = "import androidx.compose.runtime.Composable\n@Composable fun Thing() {}"

    def test_presentation_may_use_compose(self):
        self.assertEqual(check("accounts", "presentation.components", self.compose), [])
        body = "import androidx.compose.runtime.mutableStateOf"
        self.assertEqual(check("accounts", "presentation", body), [])

    def test_protocol_and_service_modules_cannot_add_compose_presentation(self):
        self.assertTrue(check("jmap", "presentation.components", self.compose))
        self.assertTrue(check("avatar", "presentation.components", self.compose))
        self.assertTrue(check("jmap", "presentation.components", "@Composable fun Card() {}"))

    def test_feature_code_lives_in_its_layers(self):
        self.assertTrue(check("accounts", "", "class Account"))
        self.assertTrue(check("settings", "widgets", "class Entry"))

    def test_shared_feature_boundaries(self):
        body = "import com.otus.designsystem.theme.OtusColors"
        self.assertTrue(check("accounts", "domain", body))
        self.assertTrue(check("accounts", "presentation", "import com.otus.jmap.JmapClient"))
        body = "import com.otus.settings.presentation.SettingsEntry"
        self.assertTrue(check("accounts", "presentation", body))

    def test_composables_live_in_components_or_screens(self):
        self.assertTrue(check("accounts", "presentation", self.compose))

    def test_screen_files_end_in_screen(self):
        path = Path("settings/src/commonMain/kotlin/com/otus/settings/presentation/screens/Page.kt")
        text = "package com.otus.settings.presentation.screens\n" + self.compose
        self.assertTrue(violations(path, text))

    def test_layer_directions(self):
        self.assertTrue(check("accounts", "presentation", "import com.otus.accounts.data.Repo"))
        self.assertTrue(check("accounts", "domain", "import com.otus.accounts.data.Repo"))
        self.assertTrue(check("accounts", "data", "import com.otus.accounts.presentation.State"))
        self.assertEqual(check("accounts", "data", "import com.otus.accounts.domain.Account"), [])

    def test_components_take_values_not_services(self):
        body = "fun row(repository: AccountRepository) {}"
        self.assertTrue(check("accounts", "presentation.components", body))

    def test_presentation_cannot_access_network(self):
        self.assertTrue(check("accounts", "presentation", "import io.ktor.client.HttpClient"))

    def test_literal_colors_belong_in_theme(self):
        self.assertTrue(check("settings", "presentation.screens", "val c = Color(0xFF000000)"))


class VisualResourceTests(unittest.TestCase):
    def test_feature_drawables_must_use_the_central_bundle(self):
        for module in ("accounts", "settings"):
            path = Path(module) / "src/commonMain/composeResources/drawable/icon.xml"
            self.assertTrue(visual_resource_violations(path))

    def test_qualified_drawable_directories_are_checked(self):
        for directory in (
            "composeResources/drawable-night",
            "composeResources/drawable-xxhdpi",
            "res/drawable-v24",
        ):
            path = Path("settings/src/commonMain") / directory / "icon.xml"
            with self.subTest(directory=directory):
                self.assertTrue(visual_resource_violations(path))
        path = Path("designsystem/src/commonMain/composeResources/drawable-night/icon.xml")
        self.assertFalse(visual_resource_violations(path))

    def test_design_system_drawables_and_data_files_are_allowed(self):
        for path in (
            "designsystem/src/commonMain/composeResources/drawable/icon.xml",
            "avatar/src/commonMain/composeResources/files/public_suffix_list.dat",
        ):
            self.assertFalse(visual_resource_violations(Path(path)))


if __name__ == "__main__":
    unittest.main()
