"""Moves changelog.d fragments into CHANGELOG.md under a new release heading."""

import sys
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UNRELEASED = "## Unreleased\n"


def release(changelog: str, fragments: list[str], version: str, day: date) -> str:
    """
    Names the Unreleased entries, with the fragments on top, as the version on the day, and
    leaves an empty Unreleased heading above them for the next release.
    """
    if UNRELEASED not in changelog:
        raise ValueError("CHANGELOG.md has no '## Unreleased' heading")
    before, after = changelog.split(UNRELEASED, 1)
    entries = "\n".join(fragment.strip() for fragment in fragments)
    body = "\n\n".join(part for part in [entries, after.strip("\n")] if part)
    return f"{before}{UNRELEASED}\n## {version} - {day.isoformat()}\n\n{body}\n"


def fragment_paths(folder: Path) -> list[Path]:
    """Every fragment, by name, so a release lists them the same way each time."""
    return sorted(path for path in folder.glob("*.md") if path.name != "README.md")


def main(version: str) -> None:
    paths = fragment_paths(ROOT / "changelog.d")
    changelog = ROOT / "CHANGELOG.md"
    text = release(
        changelog.read_text(), [path.read_text() for path in paths], version, date.today()
    )
    changelog.write_text(text)
    for path in paths:
        path.unlink()
    print(f"Released {version} with {len(paths)} fragments")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit("usage: tools/changelog.py <version>")
    main(sys.argv[1])
