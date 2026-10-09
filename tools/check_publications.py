"""Reject incomplete local Maven publications before uploading a release."""

import hashlib
import json
import sys
from pathlib import Path

TARGETS = {"": "jar", "-android": "aar", "-iosarm64": "klib", "-iossimulatorarm64": "klib"}


def violations(repository: Path, modules: list[str], version: str) -> list[str]:
    errors = []
    for module in modules:
        for suffix, extension in TARGETS.items():
            artifact = module + suffix
            directory = repository / "com" / "otus" / artifact / version
            basename = f"{artifact}-{version}"
            for ending in ("pom", "module", extension):
                path = directory / f"{basename}.{ending}"
                if not path.is_file() or path.stat().st_size == 0:
                    errors.append(f"Missing or empty artifact: {path}")
            metadata_path = directory / f"{basename}.module"
            if not metadata_path.is_file():
                continue
            try:
                metadata = json.loads(metadata_path.read_text())
                variants = metadata["variants"]
                component = metadata["component"]
                if (component["group"], component["module"], component["version"]) != (
                    "com.otus",
                    module,
                    version,
                ):
                    errors.append(f"Wrong publication coordinates: {metadata_path}")
                if not suffix:
                    references = {
                        (target["group"], target["module"], target["version"])
                        for variant in variants
                        if (target := variant.get("available-at"))
                    }
                    expected = {
                        ("com.otus", module + target, version) for target in TARGETS if target
                    }
                    if references != expected:
                        errors.append(f"Incomplete platform references: {metadata_path}")
                for variant in variants:
                    for item in variant.get("files", []):
                        path = directory / item["url"]
                        if not path.is_file():
                            errors.append(f"Missing metadata artifact: {path}")
                        elif (
                            path.stat().st_size != item["size"]
                            or hashlib.sha256(path.read_bytes()).hexdigest() != item["sha256"]
                        ):
                            errors.append(f"Artifact does not match metadata: {path}")
            except (ValueError, KeyError, TypeError):
                errors.append(f"Invalid publication metadata: {metadata_path}")
    return errors


def main() -> int:
    if len(sys.argv) != 3:
        print("Usage: check_publications.py <local Maven repository> <version>", file=sys.stderr)
        return 2
    root = Path(__file__).resolve().parents[1]
    modules = sorted(path.parent.name for path in root.glob("*/build.gradle.kts"))
    if not modules:
        print("No library modules found", file=sys.stderr)
        return 1
    errors = violations(Path(sys.argv[1]), modules, sys.argv[2])
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    print(f"Verified {len(modules) * len(TARGETS)} publications for Meta {sys.argv[2]}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
