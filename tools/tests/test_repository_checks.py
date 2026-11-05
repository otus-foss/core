"""The repository-level checks: workflow secrets, shared files, launch colors, and the config."""

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from check_architecture import library_modules
from launch_colors import LAUNCH_COLORS, launch_color_violations, launch_colors_xml
from shared_files import shared_file_violations
from workflow_secrets import shared_env, steps, workflow_secret_violations

META = Path(__file__).resolve().parents[2]

WORKFLOW = """\
jobs:
  check:
    steps:
      - name: Build
        run: ./gradlew checkAll
      - name: Sign
        env:
          KEY: ${{ secrets.KEY }}
        run: apksigner sign app.apk
"""


class WorkflowSecretTests(unittest.TestCase):
    def test_steps_and_shared_env_are_found(self):
        self.assertEqual(
            ["Build", "Sign"], [s.splitlines()[0].split(": ")[1] for s in steps(WORKFLOW)]
        )
        self.assertEqual(
            ["  A: ${{ secrets.B }}"], shared_env("env:\n  A: ${{ secrets.B }}\njobs:\n")
        )

    def test_a_secret_in_a_step_without_gradle_passes(self):
        self.assertEqual([], workflow_secret_violations(WORKFLOW))

    def test_gradle_steps_get_no_repository_secrets(self):
        text = WORKFLOW.replace("apksigner sign app.apk", "./gradlew sign")
        self.assertEqual(
            ["step 'Sign' runs Gradle with a secret"], workflow_secret_violations(text)
        )

    def test_gradle_may_publish_with_the_run_token(self):
        text = WORKFLOW.replace("secrets.KEY", "secrets.GITHUB_TOKEN").replace(
            "apksigner sign app.apk", "./gradlew publish"
        )
        self.assertEqual([], workflow_secret_violations(text))

    def test_workflow_and_job_env_share_no_secrets(self):
        for text in (
            "env:\n  TOKEN: ${{ secrets.TOKEN }}\n" + WORKFLOW,
            WORKFLOW.replace(
                "    steps:", "    env:\n      TOKEN: ${{ secrets.TOKEN }}\n    steps:"
            ),
        ):
            with self.subTest(text=text):
                self.assertTrue(workflow_secret_violations(text))

    def test_metas_own_workflows_pass(self):
        for path in sorted((META / ".github/workflows").glob("*.yml")):
            with self.subTest(workflow=path.name):
                self.assertEqual([], workflow_secret_violations(path.read_text()))


class SharedFileTests(unittest.TestCase):
    def test_copies_must_match_meta(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / ".editorconfig").write_bytes((META / ".editorconfig").read_bytes())
            (root / "ruff.toml").write_text("line-length = 80\n")
            errors = shared_file_violations(root, META)
        self.assertEqual(1, len(errors), errors)
        self.assertTrue(errors[0].startswith("ruff.toml: "))


class LaunchColorTests(unittest.TestCase):
    def test_colors_come_from_the_otus_theme_and_default_accent(self):
        xml = launch_colors_xml()
        self.assertIn('<color name="otus_bg_regular">#161617</color>', xml)
        self.assertIn('<color name="otus_text_1">#EBECED</color>', xml)
        self.assertIn('<color name="otus_accent_regular">#5A7EA4</color>', xml)

    def test_a_stale_or_missing_file_fails(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            self.assertTrue(launch_color_violations(root))
            (root / LAUNCH_COLORS).parent.mkdir(parents=True)
            (root / LAUNCH_COLORS).write_text(launch_colors_xml())
            self.assertEqual([], launch_color_violations(root))


class ConfigTests(unittest.TestCase):
    def test_library_modules_come_from_the_settings_includes(self):
        modules = library_modules(META)
        self.assertIn("jmap-push", modules)
        self.assertNotIn("build-logic", modules)


if __name__ == "__main__":
    unittest.main()
