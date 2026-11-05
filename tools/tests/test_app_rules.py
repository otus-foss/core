"""Representative legal and illegal dependency directions in Mail and Calendar."""

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from app_rules import drawable_violations, violations
from source_code import suppression_violations

LAYERS = frozenset({"app", "core", "feature"})


def check(package, body, filename="Example.kt", app="mail"):
    path = Path(f"shared/src/commonMain/kotlin/com/otus/{app}") / package.replace(".", "/")
    return violations(
        path / filename, f"package com.otus.{app}.{package}\n{body}", f"com.otus.{app}", LAYERS
    )


class AppRuleTests(unittest.TestCase):
    def test_component_cannot_import_screen_using_alias(self):
        errors = check(
            "feature.inbox.presentation.components",
            "import com.otus.mail.feature.inbox.presentation.screens.InboxScreen as View",
        )
        self.assertIn("components cannot depend on screens", errors)

    def test_domain_cannot_import_data(self):
        errors = check("feature.inbox.domain", "import com.otus.mail.feature.inbox.data.Messages")
        self.assertIn("domain cannot depend on data/presentation", errors)

    def test_domain_cannot_access_network(self):
        errors = check("feature.inbox.domain", "import io.ktor.client.HttpClient")
        self.assertIn("domain contains UI/platform/network/database dependencies", errors)

    def test_domain_and_data_cannot_use_the_design_system(self):
        for layer in ("domain", "data"):
            with self.subTest(layer=layer):
                errors = check(
                    f"feature.inbox.{layer}", "import com.otus.designsystem.theme.OtusTheme"
                )
                self.assertIn("domain and data cannot depend on the design system", errors)

    def test_presentation_cannot_import_data(self):
        errors = check(
            "feature.inbox.presentation", "import com.otus.mail.feature.inbox.data.Messages"
        )
        self.assertIn("presentation cannot depend on data implementations", errors)

    def test_feature_cannot_import_sibling(self):
        errors = check(
            "feature.inbox.domain", "import com.otus.mail.feature.contacts.domain.Person"
        )
        self.assertIn("features cannot import other features directly", errors)

    def test_core_cannot_import_features(self):
        errors = check("core.mail", "fun x() = com.otus.mail.feature.inbox.domain.Message()")
        self.assertIn("core cannot depend on app/features", errors)

    def test_component_cannot_hold_viewmodel(self):
        errors = check("feature.inbox.presentation.components", "fun row(vm: InboxViewModel) {}")
        self.assertIn(
            "elements/components take values and callbacks, not services or navigation", errors
        )

    def test_common_cannot_import_platform_roots(self):
        for reference in (
            "import android.content.Context",
            "import platform.Foundation.NSDate",
            "import java.time.Instant",
            "fun now() = java.time.Instant.now()",
        ):
            with self.subTest(reference=reference):
                self.assertIn("platform-only API in commonMain", check("core.platform", reference))

    def test_compose_platform_package_is_portable(self):
        body = "import androidx.compose.ui.platform.LocalDensity"
        self.assertFalse(check("feature.inbox.presentation.components", body))
        self.assertTrue(check("feature.inbox.domain", body))

    def test_allowed_screen_and_domain_dependency(self):
        body = "import com.otus.mail.feature.inbox.domain.Message\n@Composable fun InboxScreen() {}"
        self.assertFalse(check("feature.inbox.presentation.screens", body, "InboxScreen.kt"))

    def test_screens_end_in_screen_and_composables_stay_in_ui_layers(self):
        self.assertIn(
            "screen files must end in Screen.kt",
            check("feature.inbox.presentation.screens", "val x = 1", "Inbox.kt"),
        )
        self.assertIn(
            "composables belong in app or presentation screens and components",
            check("feature.inbox.domain", "@Composable fun Row() {}"),
        )

    def test_comments_and_strings_do_not_create_dependencies(self):
        body = (
            "// import com.otus.mail.feature.inbox.data.Messages\n"
            'val example = "com.otus.mail.feature.inbox.data.Messages"'
        )
        self.assertFalse(check("core.jmap", body))

    def test_package_must_match_path_and_layers(self):
        path = Path("shared/src/commonMain/kotlin/com/otus/mail/core/Example.kt")
        errors = violations(path, "package com.otus.mail.app", "com.otus.mail", LAYERS)
        self.assertIn("package must match source path and app namespace", errors)
        self.assertIn(
            "common production code belongs in app/core/feature", check("widgets", "val x = 1")
        )

    def test_design_system_code_belongs_in_meta(self):
        for layer in ("elements", "components", "theme"):
            with self.subTest(layer=layer):
                self.assertIn(
                    "visual elements, components, and tokens belong in otus-meta",
                    check(f"designsystem.{layer}", "val x = 1", app="calendar"),
                )

    def test_feature_adapters_may_use_meta_components(self):
        body = "import com.otus.designsystem.shared.rows.components.ContentRow"
        self.assertFalse(check("feature.inbox.presentation.components", body))

    def test_literal_colors_belong_in_meta(self):
        for body in (
            "val c = Color(0xFF202021)",
            "import androidx.compose.ui.graphics.Color as Paint\nval c = Paint(0xFF202021)",
            "val c = androidx.compose.ui.graphics.Color(0xFF202021)",
            "val c = Color(red = 0.1f, green = 0.2f, blue = 0.3f)",
            "val c = Color(255, 0, 0)",
            "val c = Color.hsv(10f, 0.5f, 0.5f)",
        ):
            with self.subTest(body=body):
                self.assertIn(
                    "literal colors belong in otus-meta designsystem/theme",
                    check("feature.inbox.presentation.components", body),
                )
        self.assertFalse(check("feature.inbox.presentation.components", "val c = OtusColors.text1"))

    def test_raw_animation_specs_are_rejected(self):
        for body in (
            "val spec = tween<Float>(300)",
            "val spec = tween<Float>(Motion.millis(speed, scale), easing = curve.easing)",
            "val spec = spring<Float>()",
            "val curve = CubicBezierEasing(0.2f, 0f, 0f, 1f)",
        ):
            with self.subTest(body=body):
                self.assertTrue(check("feature.inbox.presentation.components", body))

    def test_motion_specs_are_allowed(self):
        body = (
            "val a = motionTween<Float>(MotionSpeed.Normal, MotionCurve.Enter)\n"
            "val b = Motion.tween<Float>(speed, curve, scale)\n"
            '// tween(300)\nval text = "spring()"'
        )
        self.assertFalse(check("feature.inbox.presentation.components", body))

    def test_default_animation_specs_are_rejected(self):
        for body in (
            "suspend fun go() { pager.animateScrollToPage(it) }",
            "suspend fun go() { pager.animateScrollToPage(f(1, 2), pageOffsetFraction = 0f) }",
            "suspend fun go() { list.animateScrollBy(10f) }",
            "suspend fun go() { list.animateScrollToItem(index) }",
            'val a by animateFloatAsState(if (open) 1f else 0f, label = "fade")',
        ):
            with self.subTest(body=body):
                self.assertTrue(check("feature.inbox.presentation.components", body))

    def test_explicit_animation_specs_are_allowed(self):
        for body in (
            "suspend fun go() { pager.animateScrollToPage(it, animationSpec = turn) }",
            "suspend fun go() { pager.animateScrollToPage(it, 0f, turn) }",
            "suspend fun go() { list.animateScrollBy(10f, motionTween()) }",
            "suspend fun go() { list.animateScrollToItem(index, turn) }",
            "val a by animateFloatAsState(if (open) 1f else 0f, motionTween()) { done() }",
        ):
            with self.subTest(body=body):
                self.assertFalse(check("feature.inbox.presentation.components", body))

    def test_suppression_is_rejected(self):
        for annotation in (
            '@Suppress("ALL")',
            '@SuppressLint("NewApi")',
            '@android.annotation.SuppressLint("NewApi")',
            '@kotlin.Suppress("ALL")',
            '@file:kotlin.Suppress("ALL")',
            '@get:Suppress("ALL")',
            '@[Suppress("ALL")]',
        ):
            with self.subTest(annotation=annotation):
                self.assertIn(
                    "inline lint suppression is prohibited; fix the violation",
                    check("core.jmap", f"{annotation}\nfun x() {{}}"),
                )

    def test_xml_tools_ignore_is_rejected(self):
        ignored = '<application tools:ignore="GoogleAppIndexingWarning" />'
        self.assertTrue(suppression_violations(ignored))
        self.assertFalse(suppression_violations('<application android:label="@string/app" />'))

    def test_template_code_in_strings_is_checked(self):
        for body in (
            'val x = "${com.otus.mail.feature.inbox.data.Messages()}"',
            'val x = """a ${"b" + com.otus.mail.feature.inbox.data.Messages()} c"""',
        ):
            with self.subTest(body=body):
                self.assertTrue(check("feature.inbox.domain", body))
        self.assertFalse(
            check("feature.inbox.domain", 'val x = "${size} com.otus.mail.feature.inbox.data.X"')
        )

    def test_domain_cannot_use_compose_resources(self):
        resources = "import org.jetbrains.compose.resources.stringResource"
        self.assertTrue(check("feature.inbox.domain", resources))

    def test_drawables_are_rejected_in_every_source_set(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            for name in (
                "shared/src/commonMain/composeResources/drawable/a.xml",
                "shared/src/androidMain/composeResources/drawable-night/b.png",
                "androidApp/src/main/res/drawable-v24/c.xml",
                "androidApp/src/main/res/values/colors.xml",
            ):
                (root / name).parent.mkdir(parents=True, exist_ok=True)
                (root / name).write_text("<x />")
            errors = drawable_violations(root)
        self.assertEqual(3, len(errors), errors)
        self.assertFalse(any("values" in error for error in errors))


if __name__ == "__main__":
    unittest.main()
