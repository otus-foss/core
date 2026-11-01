"""Regression tests showing representative legal/illegal library dependency directions."""

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_architecture import source_code, violations, visual_resource_violations


def check(module, package, body, source_set="commonMain", ui_modules=frozenset({"designsystem"})):
    base = "com.otus." + module.replace("-", ".")
    name = base + (f".{package}" if package else "")
    path = Path(f"{module}/src/{source_set}/kotlin") / name.replace(".", "/") / "Example.kt"
    return violations(path, f"package {name}\n{body}", ui_modules)


def reference_bodies(reference):
    return (
        f"import {reference}",
        f"import {reference} as Item\nfun value() = Item()",
        f"fun value() = {reference}()",
        f'val value = "${{{reference}()}}"',
    )


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
        self.assertEqual(check("designsystem", "shared.controls.elements", body), [])

    def test_code_outside_layers_is_rejected(self):
        self.assertTrue(check("designsystem", "widgets", "fun x() = 1"))
        self.assertTrue(check("designsystem", "", "fun x() = 1"))

    def test_elements_cannot_import_components(self):
        body = "import com.otus.designsystem.shared.navigation.components.Sidebar"
        self.assertTrue(check("designsystem", "shared.controls.elements", body))
        self.assertEqual(check("designsystem", "shared.controls.components", body), [])

    def test_cannot_depend_on_protocol_or_feature_modules(self):
        self.assertTrue(
            check("designsystem", "shared.controls.elements", "import com.otus.jmap.JmapClient")
        )
        self.assertTrue(
            check("designsystem", "shared.controls.components", "import io.ktor.client.HttpClient")
        )
        body = "import com.otus.accounts.domain.ConnectedAccount"
        self.assertTrue(check("designsystem", "shared.controls.components", body))

    def test_may_format_dates(self):
        body = "import com.otus.dateformat.DateFormatter"
        self.assertEqual(check("designsystem", "shared.controls.components", body), [])
        self.assertTrue(
            check(
                "designsystem", "shared.controls.components", "import com.otus.dateformatting.Store"
            )
        )

    def test_components_take_values_not_services(self):
        self.assertTrue(
            check("designsystem", "shared.controls.components", "fun row(vm: InboxViewModel) {}")
        )

    def test_literal_colors_belong_in_theme(self):
        self.assertTrue(
            check("designsystem", "shared.controls.elements", "val c = Color(0xFFFFFFFF)")
        )
        self.assertEqual(check("designsystem", "theme", "val c = Color(0xFFFFFFFF)"), [])

    def test_aliased_and_decimal_literal_colors_belong_in_theme(self):
        aliased = "import androidx.compose.ui.graphics.Color as Tint\nval c = Tint(0xFFFFFFFF)"
        self.assertTrue(check("designsystem", "shared.controls.elements", aliased))
        self.assertTrue(
            check("designsystem", "shared.controls.elements", "val c = Color(red = 0.5f, 0f, 0f)")
        )
        self.assertTrue(
            check("designsystem", "shared.controls.elements", "val c = Color(255, 0, 0)")
        )
        token = "val c = Color(OtusColors.text1.value)"
        self.assertEqual(check("designsystem", "shared.controls.elements", token), [])

    def test_portable_compose_platform_package_is_allowed(self):
        body = "import androidx.compose.ui.platform.LocalDensity"
        self.assertEqual(check("designsystem", "shared.controls.elements", body), [])


class ThemePackageTests(unittest.TestCase):
    packages = ("theme", "theme.colors", "theme.tokens.inbox", "theme.motion")

    def test_root_theme_and_responsibility_packages_allow_literal_colors(self):
        for package in (
            *self.packages,
            "theme.colors.palette",
            "theme.typography",
            "theme.spacing._scale2",
        ):
            for body in (
                "val tint = Color(0xFFFFFFFF)",
                "val tint = androidx.compose.ui.graphics.Color(255, 0, 0)",
                "import androidx.compose.ui.graphics.Color as Tint\nval tint = Tint(red = 0.5f)",
            ):
                with self.subTest(package=package, body=body):
                    self.assertEqual(check("designsystem", package, body), [])

    def test_theme_dependencies_and_root_entry_point_are_allowed(self):
        for package in self.packages:
            for target in (
                "designsystem.theme.OtusTheme",
                "designsystem.theme.colors.OtusColors",
                "designsystem.theme.tokens.inbox.InboxTokens",
                "designsystem.theme.motion.Motion",
                "designsystem.resources.Res",
                "dateformat.DateFormatter",
            ):
                for body in reference_bodies(f"com.otus.{target}"):
                    with self.subTest(package=package, body=body):
                        self.assertEqual(check("designsystem", package, body), [])
        self.assertEqual(check("designsystem", "theme", "@Composable fun OtusTheme() {}"), [])

    def test_nested_theme_groups_are_organizational(self):
        for package in ("theme.shared.controls.elements", "theme.tokens.elements"):
            for body in reference_bodies("com.otus.designsystem.theme.tokens.components.RowTokens"):
                with self.subTest(package=package, body=body):
                    self.assertEqual(check("designsystem", package, body), [])
        self.assertEqual(
            check(
                "designsystem",
                "shared.controls.elements",
                "import com.otus.designsystem.theme.tokens.components.RowTokens",
            ),
            [],
        )

    def test_theme_like_and_malformed_paths_cannot_define_colors(self):
        for package in (
            "themeish",
            "themeish.colors",
            "mail.inbox.theme",
            "mail.inbox.theme.colors",
            "mail.inbox.components.theme",
            "shared.theme.elements",
            "theme.2colors",
            "theme.colors.2palette",
        ):
            with self.subTest(package=package):
                errors = check("designsystem", package, "val tint = Color(0xFFFFFFFF)")
                self.assertIn("literal colors belong in theme", errors)
                self.assertTrue(any("design-system code belongs" in error for error in errors))

    def test_other_modules_cannot_define_theme_colors(self):
        for module in ("settings", "jmap", "designsystemish"):
            with self.subTest(module=module):
                errors = check(module, "theme.colors", "val tint = Color(0xFFFFFFFF)")
                self.assertIn("literal colors belong in theme", errors)

    def test_nested_theme_keeps_platform_and_catch_all_rules(self):
        for source_set, reference in (
            ("androidMain", "android.content.Context"),
            ("androidMain", "java.time.Instant"),
            ("androidMain", "javax.net.SocketFactory"),
            ("iosMain", "platform.Foundation.NSDate"),
        ):
            for body in reference_bodies(reference):
                with self.subTest(source_set=source_set, body=body):
                    self.assertEqual(check("designsystem", "theme.motion", body, source_set), [])
                    self.assertEqual(
                        check("designsystem", "theme.motion", body),
                        ["platform-only API in commonMain"],
                    )
        for package in ("theme.utils", "theme.tokens.helpers", "theme.colors.manager"):
            with self.subTest(package=package):
                self.assertEqual(
                    check("designsystem", package, "val tint = Color(0xFFFFFFFF)"),
                    ["use responsibility-specific packages instead of catch-all packages"],
                )

    def test_nested_theme_keeps_package_and_namespace_matching(self):
        for directory, package in (
            ("com.otus.designsystem.theme.colors", "com.otus.designsystem.theme.motion"),
            ("com.otus.designsystem.theme.colors", "com.otus.designsystem.theme..colors"),
            ("com.otus.designsystemish.theme.colors", "com.otus.designsystemish.theme.colors"),
            ("com.otus.designsystemtheme.colors", "com.otus.designsystemtheme.colors"),
            ("org.example.theme.colors", "org.example.theme.colors"),
        ):
            path = Path("designsystem/src/commonMain/kotlin") / directory.replace(".", "/") / "A.kt"
            with self.subTest(directory=directory, package=package):
                errors = violations(path, f"package {package}\nval tint = Color(0xFFFFFFFF)")
                self.assertIn(
                    "package must match source path and the module namespace com.otus.designsystem",
                    errors,
                )
                self.assertIn("literal colors belong in theme", errors)


class QualifiedExpressionTests(unittest.TestCase):
    forbidden_references = (
        (
            "theme.brand",
            "com.otus.designsystem.shared.controls.elements.WIDTH",
            "theme cannot depend on scoped UI",
        ),
        (
            "shared.controls.elements",
            "com.otus.designsystem.shared.controls.components.WIDTH",
            "elements cannot depend on components",
        ),
        (
            "shared.controls.components",
            "com.otus.designsystem.mail.inbox.elements.WIDTH",
            "shared UI cannot depend on mail or calendar UI",
        ),
        (
            "mail.inbox.components",
            "com.otus.designsystem.calendar.events.elements.WIDTH",
            "mail and calendar UI cannot depend on each other",
        ),
        (
            "calendar.events.components",
            "com.otus.designsystem.mail.inbox.elements.WIDTH",
            "mail and calendar UI cannot depend on each other",
        ),
        (
            "theme.brand",
            "com.otus.jmap.LIMIT",
            "design-system modules may depend only on design-system and dateformat",
        ),
        (
            "theme.brand",
            "com.otus.mail.app.LIMIT",
            "shared libraries cannot depend on Mail or Calendar app code",
        ),
        (
            "theme.brand",
            "android.os.Build.VERSION_CODES.TIRAMISU",
            "platform-only API in commonMain",
        ),
        (
            "theme.brand",
            "io.ktor.http.HttpStatusCode.OK.value",
            "UI must not access network or database APIs",
        ),
    )

    def test_closed_and_open_ended_theme_ranges_are_allowed(self):
        modules = frozenset({"designsystem", "designsystem-controls"})
        for namespace in ("designsystem", "designsystem.controls"):
            for theme in ("theme", "theme.brand"):
                base = f"com.otus.{namespace}.{theme}"
                for operator in ("..", "..<", " .. ", " ..< ", "..\n    ", "..<\n    "):
                    expression = f"{base}.OWL_ART_WIDTH{operator}{base}.OWL_ART_HEIGHT"
                    for module, package in (
                        ("designsystem", "theme.brand"),
                        ("designsystem", "shared.controls.elements"),
                        ("accounts", "presentation.components"),
                    ):
                        with self.subTest(module=module, package=package, expression=expression):
                            self.assertEqual(
                                check(
                                    module, package, f"val value = {expression}", ui_modules=modules
                                ),
                                [],
                            )

    def test_qualified_theme_tokens_and_navigation_can_span_lines(self):
        for reference in (
            "com.otus.designsystem.theme.OWL_ART_WIDTH",
            "com.otus.designsystem.theme.brand.OWL_ART_WIDTH",
            "com.otus.designsystem.theme.tokens.inbox.InboxTokens.row.height.value",
        ):
            for separator in (".\n    ", "\n    .", " \n . \n "):
                expressions = (
                    reference.replace(".", separator),
                    f"{reference}{separator}toInt()",
                    f"{reference}{separator}coerceAtLeast(1)",
                )
                for expression in expressions:
                    with self.subTest(expression=expression):
                        self.assertEqual(
                            check(
                                "designsystem", "shared.controls.elements", f"val n = {expression}"
                            ),
                            [],
                        )

    def test_forbidden_references_are_checked_on_each_range_endpoint(self):
        allowed = "com.otus.designsystem.theme.brand.OWL_ART_WIDTH"
        for package, forbidden, error in self.forbidden_references:
            for operator in ("..", "..<"):
                for left, right in ((allowed, forbidden), (forbidden, allowed)):
                    body = f"val value = {left}{operator}{right}"
                    with self.subTest(package=package, body=body):
                        self.assertIn(error, check("designsystem", package, body))

    def test_forbidden_references_are_checked_across_chained_lines(self):
        for package, reference, error in self.forbidden_references:
            for separator in (".\n    ", "\n    .", " \n . \n "):
                expression = reference.replace(".", separator)
                for body in (f"val value = {expression}", f'val label = "${{{expression}}}"'):
                    with self.subTest(package=package, body=body):
                        self.assertIn(error, check("designsystem", package, body))

    def test_import_punctuation_is_not_treated_as_expression_navigation(self):
        for target in (
            "theme..colors.OtusColors",
            "theme.colors..OtusColors",
            "theme.colors.OtusColors.",
            "theme.colors.2palette.OtusColors",
        ):
            for suffix in ("", " as Item"):
                body = f"import com.otus.designsystem.{target}{suffix}"
                with self.subTest(body=body):
                    errors = check("designsystem", "shared.controls.elements", body)
                    self.assertIn(
                        "design-system references must use <scope>.<group>.elements/components "
                        "or theme/resources",
                        errors,
                    )


class ScopedDesignSystemTests(unittest.TestCase):
    def test_scoped_layers_and_root_theme_are_allowed(self):
        packages = [
            f"{scope}.controls.{layer}"
            for scope in ("shared", "mail", "calendar")
            for layer in ("elements", "components")
        ]
        for package in [*packages, "shared.calendar.components", "theme"]:
            with self.subTest(package=package):
                self.assertEqual(check("designsystem", package, "@Composable fun Row() {}"), [])

    def test_flat_and_malformed_production_paths_are_rejected(self):
        for package in (
            "elements",
            "components",
            "elements.controls",
            "components.mail",
            "controls.elements",
            "shared",
            "shared.elements",
            "shared.controls",
            "shared.controls.widgets",
            "shared.elements.controls",
            "shared.elements.components",
            "shared.controls.elements.nested",
            "shared.controls.nested.components",
            "shared.theme.elements",
            "shared.resources.components",
            "shared.2controls.elements",
            "mail.inbox.theme",
            "calendar.events",
            "other.controls.elements",
            "resources",
        ):
            with self.subTest(package=package):
                errors = check("designsystem", package, "fun value() = 1")
                self.assertTrue(any("design-system code belongs" in error for error in errors))

    def test_permitted_dependency_directions(self):
        for scope in ("shared", "mail", "calendar"):
            for layer in ("elements", "components"):
                targets = [
                    "theme.OtusColors",
                    "theme.colors.OtusColors",
                    "theme.tokens.inbox.InboxTokens",
                    "theme.motion.Motion",
                    "resources.Res",
                    "resources.icon",
                    "shared.controls.elements.Button",
                    f"{scope}.other.elements.Label",
                ]
                if layer == "components":
                    targets += [
                        "shared.controls.components.Row",
                        f"{scope}.other.components.Panel",
                    ]
                for target in targets:
                    with self.subTest(scope=scope, layer=layer, target=target):
                        self.assertEqual(
                            check(
                                "designsystem",
                                f"{scope}.controls.{layer}",
                                f"import com.otus.designsystem.{target}",
                            ),
                            [],
                        )

    def test_elements_cannot_use_components_in_any_group(self):
        for scope in ("shared", "mail", "calendar"):
            for body in (
                "import com.otus.designsystem.shared.navigation.components.Sidebar as Panel",
                f"fun row() = com.otus.designsystem.{scope}.other.components.Row()",
            ):
                with self.subTest(scope=scope, body=body):
                    errors = check("designsystem", f"{scope}.controls.elements", body)
                    self.assertEqual(errors, ["elements cannot depend on components"])

    def test_shared_and_app_scope_boundaries(self):
        for scope, target in (
            ("shared", "mail"),
            ("shared", "calendar"),
            ("mail", "calendar"),
            ("calendar", "mail"),
        ):
            for body in (
                f"import com.otus.designsystem.{target}.rows.elements.Label as Item",
                f"fun row() = com.otus.designsystem.{target}.rows.elements.Label()",
                f'val label = "${{com.otus.designsystem.{target}.rows.elements.Label()}}"',
            ):
                with self.subTest(scope=scope, target=target, body=body):
                    errors = check("designsystem", f"{scope}.controls.components", body)
                    expected = (
                        "shared UI cannot depend on mail or calendar UI"
                        if scope == "shared"
                        else "mail and calendar UI cannot depend on each other"
                    )
                    self.assertEqual(errors, [expected])

    def test_theme_cannot_depend_on_scoped_ui(self):
        for package in ThemePackageTests.packages:
            for scope in ("shared", "mail", "calendar"):
                for layer in ("elements", "components"):
                    target = f"com.otus.designsystem.{scope}.controls.{layer}.Row"
                    for body in reference_bodies(target):
                        with self.subTest(package=package, body=body):
                            self.assertEqual(
                                check("designsystem", package, body),
                                ["theme cannot depend on scoped UI"],
                            )

    def test_legacy_and_malformed_imports_are_rejected_in_production_consumers(self):
        for module, package in (
            ("designsystem", "shared.controls.components"),
            ("accounts", "presentation.components"),
        ):
            for target in (
                "elements.Button",
                "components.Row",
                "shared.elements.Button",
                "shared.controls.Button",
                "shared.controls.widgets.Button",
                "shared.components.elements.Button",
                "unknown.controls.elements.Button",
                "themeish.colors.OtusColors",
                "theme.2colors.OtusColors",
                "theme.colors.2palette.OtusColors",
                "theme..colors.OtusColors",
                "mail.inbox.theme.OtusColors",
            ):
                for body in (
                    f"import com.otus.designsystem.{target} as Item",
                    f"fun row() = com.otus.designsystem.{target}()",
                ):
                    with self.subTest(module=module, target=target, body=body):
                        errors = check(module, package, body)
                        self.assertTrue(
                            any("design-system references must use" in e for e in errors)
                        )

    def test_values_and_callbacks_are_allowed_but_services_are_rejected(self):
        for scope in ("shared", "mail", "calendar"):
            for layer in ("elements", "components"):
                package = f"{scope}.controls.{layer}"
                body = "fun row(title: String, onClick: () -> Unit) {}"
                self.assertEqual(check("designsystem", package, body), [])
                for body in (
                    "fun row(model: InboxViewModel) {}",
                    "fun row(repository: MessageRepository) {}",
                    "fun row(controller: NavController) {}",
                    "import androidx.navigation.NavController as Navigator",
                    "val model = viewModel<InboxState>()",
                    "val service = koinInject<Service>()",
                    "val model = hiltViewModel<InboxState>()",
                ):
                    with self.subTest(package=package, body=body):
                        self.assertEqual(
                            check("designsystem", package, body),
                            [
                                "elements/components take values and callbacks, "
                                "not services or navigation"
                            ],
                        )

    def test_network_storage_features_and_app_references_remain_rejected(self):
        for reference, error in (
            ("io.ktor.client.HttpClient", "UI must not access network or database APIs"),
            ("app.cash.sqldelight.Query", "UI must not access network or database APIs"),
            ("androidx.room.Room", "UI must not access network or database APIs"),
            ("okhttp3.OkHttpClient", "UI must not access network or database APIs"),
            ("com.otus.jmap.JmapClient", "design-system modules may depend only"),
            ("com.otus.accounts.domain.Account", "design-system modules may depend only"),
            ("com.otus.settings.domain.Entry", "design-system modules may depend only"),
            ("com.otus.mail.app.App", "shared libraries cannot depend on Mail or Calendar"),
            ("com.otus.calendar.app.App", "shared libraries cannot depend on Mail or Calendar"),
        ):
            for package in ("mail.inbox.components", *ThemePackageTests.packages):
                for body in reference_bodies(reference):
                    with self.subTest(package=package, body=body):
                        errors = check("designsystem", package, body)
                        self.assertTrue(any(message.startswith(error) for message in errors))

    def test_nested_platform_paths_and_literal_colors(self):
        package = "mail.message.elements"
        body = "import android.content.Context"
        self.assertEqual(check("designsystem", package, body, "androidMain"), [])
        self.assertEqual(check("designsystem", package, body), ["platform-only API in commonMain"])
        body = "import platform.Foundation.NSDate"
        self.assertEqual(check("designsystem", package, body, "iosMain"), [])
        for package in (
            "shared.controls.elements",
            "mail.message.components",
            "shared.theme.elements",
        ):
            with self.subTest(package=package):
                errors = check("designsystem", package, "val tint = Color(0xFFFFFFFF)")
                self.assertIn("literal colors belong in theme", errors)

    def test_split_design_system_modules_keep_dependency_rules(self):
        modules = frozenset({"designsystem", "designsystem-controls"})
        for module, target_base in (
            ("designsystem", "com.otus.designsystem.controls"),
            ("designsystem-controls", "com.otus.designsystem"),
        ):
            for package, target, error in (
                ("shared.forms.components", "shared.controls.elements.Button", None),
                ("shared.forms.components", "resources.Res", None),
                ("shared.forms.elements", "theme.colors.OtusColors", None),
                ("shared.forms.components", "theme.tokens.inbox.InboxTokens", None),
                ("theme.colors", "theme.motion.Motion", None),
                (
                    "shared.forms.elements",
                    "shared.controls.components.Row",
                    "elements cannot depend",
                ),
                ("shared.forms.components", "mail.inbox.elements.Label", "shared UI cannot depend"),
                ("mail.forms.components", "calendar.events.elements.Label", "mail and calendar UI"),
                ("calendar.forms.components", "mail.inbox.elements.Label", "mail and calendar UI"),
                ("theme", "shared.controls.elements.Button", "theme cannot depend"),
                ("theme.colors", "shared.controls.elements.Button", "theme cannot depend"),
                ("theme.tokens.inbox", "mail.inbox.components.Row", "theme cannot depend"),
                ("theme.motion", "calendar.events.elements.Label", "theme cannot depend"),
                ("shared.forms.components", "elements.Button", "design-system references must use"),
                ("shared.forms.components", "components.Row", "design-system references must use"),
            ):
                for body in reference_bodies(f"{target_base}.{target}"):
                    with self.subTest(module=module, package=package, body=body):
                        errors = check(module, package, body, ui_modules=modules)
                        if error is None:
                            self.assertEqual(errors, [])
                        else:
                            self.assertTrue(any(message.startswith(error) for message in errors))
            self.assertEqual(
                check(module, "theme.colors", "val tint = Color(0xFFFFFFFF)", ui_modules=modules),
                [],
            )

    def test_comments_and_strings_do_not_count_as_references(self):
        body = """
// import com.otus.designsystem.mail.inbox.components.Row
/** com.otus.designsystem.components.Sidebar */
val example = "com.otus.designsystem.calendar.events.components.Row"
"""
        self.assertEqual(check("designsystem", "shared.controls.elements", body), [])

    def test_reference_on_the_package_line_is_still_checked(self):
        package = "com.otus.designsystem.shared.controls.elements"
        path = Path("designsystem/src/commonMain/kotlin") / package.replace(".", "/") / "Test.kt"
        text = f"package {package}; import com.otus.designsystem.shared.rows.components.Row"
        self.assertIn("elements cannot depend on components", violations(path, text))

    def test_tests_keep_the_existing_production_guard_exemption(self):
        body = """
import io.ktor.client.HttpClient
import android.content.Context
import com.otus.designsystem.mail.inbox.components.Row
import com.otus.designsystem.components.OldFixture
fun row(model: InboxViewModel) = Color(0xFFFFFFFF)
"""
        for source_set in ("commonTest", "androidHostTest", "iosTest"):
            for package in ("", "shared.controls.elements", *ThemePackageTests.packages):
                with self.subTest(source_set=source_set, package=package):
                    self.assertEqual(check("designsystem", package, body, source_set), [])

    def test_test_sources_still_enforce_packages_apps_suppressions_and_import_style(self):
        path = Path(
            "designsystem/src/commonTest/kotlin/com/otus/designsystem/shared/controls/elements/Test.kt"
        )
        errors = violations(path, "package com.otus.designsystem.shared.controls.components")
        self.assertTrue(any("package must match" in error for error in errors))
        for body, error in (
            ("import com.otus.mail.app.App", "shared libraries cannot depend"),
            ("import com.otus.designsystem.shared.controls.components.*", "wildcard imports"),
            ('@Suppress("unused") fun test() {}', "inline lint suppression"),
            ("\n" * 301, "file has"),
        ):
            with self.subTest(body=body):
                errors = check("designsystem", "shared.controls.elements", body, "commonTest")
                self.assertTrue(any(message.startswith(error) for message in errors))


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


class MotionTests(unittest.TestCase):
    raw = "animate through Motion; springs and curves belong in theme/motion"
    easing = "a tween outside theme/motion must name a MotionCurve easing"
    literal = "tween durations come from Motion, not number literals"

    def test_specs_that_bypass_motion_are_rejected(self):
        for body, error in (
            ("fun bar() = fadeIn(tween(morph.millis))", self.easing),
            ("val spec = tween<Float>(240, easing = MotionCurve.Standard.easing)", self.literal),
            ("val spec = TweenSpec<Float>(durationMillis = 300, easing = Linear)", self.literal),
            ("val spec = tween<Float>(\n    millis,\n    delayMillis = 40,\n)", self.easing),
            ("val spec = spring<Float>(stiffness = Spring.StiffnessLow)", self.raw),
            ("val curve = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)", self.raw),
        ):
            for module, package in (
                ("designsystem", "shared.bars.components"),
                ("settings", "presentation.components"),
            ):
                with self.subTest(body=body, module=module):
                    self.assertIn(error, check(module, package, body))

    def test_motion_specs_and_named_easings_pass(self):
        for body in (
            "val spec = Motion.tween<Float>(MotionSpeed.Fast, MotionCurve.Exit, scale)",
            "val spec = motionTween<Float>(MotionSpeed.Quick)",
            "fun bar() = fadeIn(Motion\n    .tween(scale = scale))",
            "val clock = tween<Float>(morph.millis, easing = LinearEasing)",
            "val fade = tween<Float>(rest, delayMillis = gap, easing = MotionCurve.Enter.easing)",
            "val owl = TweenSpec<Float>(Motion.scaled(length, scale), easing = LinearEasing)",
            '// tween(240)\nval label = "spring(1)"',
        ):
            with self.subTest(body=body):
                self.assertEqual(check("designsystem", "shared.bars.components", body), [])

    def test_calls_left_on_their_default_spring_are_rejected(self):
        for body, name in (
            ("suspend fun go() = scroll.animateScrollToItem(selected)", "animateScrollToItem"),
            ("suspend fun go() = pager.animateScrollToPage(page)", "animateScrollToPage"),
            ("suspend fun go() = list.animateScrollBy(100f)", "animateScrollBy"),
            ('val fill by animateColorAsState(color, label = "Fill")', "animateColorAsState"),
        ):
            with self.subTest(body=body):
                self.assertIn(
                    f"give {name} an animationSpec from Motion",
                    check("designsystem", "shared.bars.components", body),
                )

    def test_calls_given_a_motion_spec_pass(self):
        for body in (
            "suspend fun go() = scroll.animateScrollToItem(selected, turn)",
            "suspend fun go() = pager.animateScrollToPage(page, animationSpec = turn)",
            "suspend fun go() = list.animateScrollBy(100f, motionTween())",
            'val fill by animateColorAsState(color, motionTween(), label = "Fill")',
            "val size by animateDpAsState(\n    target,\n    animationSpec = spec,\n)",
        ):
            with self.subTest(body=body):
                self.assertEqual(check("designsystem", "shared.bars.components", body), [])

    def test_theme_motion_defines_curves_and_tweens(self):
        body = "val curve = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)\nfun <T> base() = tween<T>(240)"
        self.assertEqual(check("designsystem", "theme.motion", body), [])
        self.assertIn(self.raw, check("designsystem", "theme.colors", body))


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
