"""Changelog fragments land under the release heading, above the older Unreleased entries."""

import sys
import tempfile
import unittest
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from changelog import fragment_paths, release

DAY = date(2026, 11, 28)
CHANGELOG = "# Changelog\n\n## Unreleased\n\n- Old entry.\n\n## 0.7.0 - 2026-10-11\n\n- Older.\n"


class ChangelogTests(unittest.TestCase):
    def test_fragments_go_above_the_unreleased_entries_under_the_version(self):
        self.assertEqual(
            "# Changelog\n\n## Unreleased\n\n## 0.8.0 - 2026-11-28\n\n"
            "- First.\n- Second.\n\n- Old entry.\n\n## 0.7.0 - 2026-10-11\n\n- Older.\n",
            release(CHANGELOG, ["- First.\n", "\n- Second.\n\n"], "0.8.0", DAY),
        )

    def test_a_release_without_fragments_names_the_unreleased_entries(self):
        self.assertEqual(
            "# Changelog\n\n## Unreleased\n\n## 0.8.0 - 2026-11-28\n\n"
            "- Old entry.\n\n## 0.7.0 - 2026-10-11\n\n- Older.\n",
            release(CHANGELOG, [], "0.8.0", DAY),
        )

    def test_a_changelog_without_unreleased_is_refused(self):
        with self.assertRaises(ValueError):
            release("# Changelog\n", ["- First.\n"], "0.8.0", DAY)

    def test_fragments_are_every_markdown_file_but_the_readme_by_name(self):
        with tempfile.TemporaryDirectory() as folder:
            for name in ["b-two.md", "a-one.md", "README.md", "notes.txt"]:
                (Path(folder) / name).write_text("- Entry.\n")
            self.assertEqual(
                ["a-one.md", "b-two.md"], [path.name for path in fragment_paths(Path(folder))]
            )


if __name__ == "__main__":
    unittest.main()
