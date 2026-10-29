"""Reject incomplete local Maven publications before uploading a release."""

import hashlib
import json
import sys
from pathlib import Path
from xml.etree import ElementTree

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
            pom_path = directory / f"{basename}.pom"
            if pom_path.is_file() and pom_path.stat().st_size:
                errors.extend(pom_violations(pom_path, version))
            metadata_path = directory / f"{basename}.module"
            if metadata_path.is_file():
                errors.extend(metadata_violations(metadata_path, module, suffix, version))
    return errors


def metadata_violations(path: Path, module: str, suffix: str, version: str) -> list[str]:
    """Check Gradle module metadata against its coordinates, platform variants, and files."""
    errors = []
    try:
        metadata = json.loads(path.read_text())
        variants = metadata["variants"]
        component = metadata["component"]
        if (component["group"], component["module"], component["version"]) != (
            "com.otus",
            module,
            version,
        ):
            errors.append(f"Wrong publication coordinates: {path}")
        if not suffix:
            references = {
                (target["group"], target["module"], target["version"])
                for variant in variants
                if (target := variant.get("available-at"))
            }
            expected = {("com.otus", module + target, version) for target in TARGETS if target}
            if references != expected:
                errors.append(f"Incomplete platform references: {path}")
        if any(
            spec.get("requires") != version or spec.get("strictly", version) != version
            for spec in otus_module_dependencies(variants)
        ):
            errors.append(f"Dependency on another com.otus version: {path}")
        for variant in variants:
            for item in variant.get("files", []):
                artifact = path.parent / item["url"]
                if not artifact.is_file():
                    errors.append(f"Missing metadata artifact: {artifact}")
                elif (
                    artifact.stat().st_size != item["size"]
                    or hashlib.sha256(artifact.read_bytes()).hexdigest() != item["sha256"]
                ):
                    errors.append(f"Artifact does not match metadata: {artifact}")
    except (ValueError, KeyError, TypeError):
        errors.append(f"Invalid publication metadata: {path}")
    return errors


def otus_module_dependencies(variants: list[dict]) -> list[dict]:
    """The version of every com.otus dependency and constraint in Gradle module metadata."""
    return [
        entry.get("version", {})
        for variant in variants
        for key in ("dependencies", "dependencyConstraints")
        for entry in variant.get(key, [])
        if entry["group"] == "com.otus"
    ]


def pom_violations(path: Path, version: str) -> list[str]:
    """Modules release together, so a POM may only name com.otus artifacts of this release."""
    try:
        root = ElementTree.fromstring(path.read_bytes())
    except ElementTree.ParseError:
        return [f"Invalid POM: {path}"]
    versions = [
        dependency.findtext("{*}version")
        for dependency in root.iterfind(".//{*}dependency")
        if dependency.findtext("{*}groupId") == "com.otus"
    ]
    if any(found != version for found in versions):
        return [f"Dependency on another com.otus version: {path}"]
    return []


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
