"""Configuration files that editors and tools find only at a repository's root.

`.editorconfig` and `ruff.toml` must sit in each repository, so Mail and Calendar keep copies of
Meta's. The architecture check fails when a copy drifts.
"""

from pathlib import Path

SHARED_FILES = (".editorconfig", "ruff.toml")


def shared_file_violations(root: Path, meta: Path) -> list[str]:
    return [
        f"{name}: differs from otus-meta's copy; copy {meta / name} here"
        for name in SHARED_FILES
        if not (root / name).is_file() or (root / name).read_bytes() != (meta / name).read_bytes()
    ]
