"""Fail on source-layout, dependency-policy, and shared-configuration violations.

Every Otus repository runs this copy from its core checkout, from its own root. The
repository's config/checks.toml says what it is:

    namespace = "com.otusfoss.mail"     # the app's package root; Core uses "com.otusfoss.core"
    kind = "app"                    # "app" or "library"
    layers = ["app", "core", "feature"]   # an app's top-level packages in shared/commonMain
"""

import sys
import tomllib
from pathlib import Path

from app_rules import app_violations
from launch_colors import launch_color_violations
from library_rules import library_violations
from shared_files import shared_file_violations
from workflow_secrets import workflow_violations

CORE = Path(__file__).resolve().parents[1]


def library_modules(root: Path) -> list[str]:
    """The modules settings.gradle.kts includes, such as `:jmap-push`."""
    settings = (root / "settings.gradle.kts").read_text()
    return sorted(
        line.split('"')[1].removeprefix(":")
        for line in settings.splitlines()
        if line.startswith('include(":')
    )


def repository_violations(root: Path) -> list[str]:
    config = tomllib.loads((root / "config/checks.toml").read_text())
    if config["kind"] == "library":
        failures = library_violations(root, library_modules(root))
    else:
        layers = frozenset(config["layers"])
        failures = app_violations(root, config["namespace"], layers)
        failures += launch_color_violations(root)
        failures += shared_file_violations(root, CORE)
    return failures + workflow_violations(root)


def main() -> int:
    root = Path.cwd()
    failures = repository_violations(root)
    if failures:
        print("\n".join(failures), file=sys.stderr)
        return 1
    print("Architecture checks passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
