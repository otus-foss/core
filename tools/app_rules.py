"""Source-layout and dependency rules for Mail and Calendar; see each app's docs/architecture.md."""

import re
from pathlib import Path

from file_length import file_length_violations
from motion_specs import motion_violations
from source_code import (
    CATCH_ALL_PACKAGES,
    COMPOSABLE,
    DATA_APIS,
    PLATFORM_APIS,
    SERVICE_TYPES,
    UI_APIS,
    is_production,
    kotlin_header_violations,
    literal_color,
    source_code,
    source_package,
    suppression_violations,
    uses,
)

SOURCE_FOLDERS = ("shared/src", "androidApp/src", "iosApp")
FEATURE_LAYERS = {"domain", "data", "presentation"}
DRAWABLE = re.compile(r"drawable(?:-[\w-]+)?")


def violations(path: Path, text: str, base: str, layers: frozenset[str]) -> list[str]:
    """Errors for one app file; base is the app namespace and layers its top-level packages."""
    errors = file_length_violations(path, text) + suppression_violations(text)
    if path.suffix != ".kt":
        return errors
    code = source_code(text)
    package, header_errors = kotlin_header_violations(path.parts, code)
    if package is None:
        return [*errors, *header_errors]
    errors += header_errors
    expected = source_package(path.parts)
    if package != expected or not expected.startswith(base + ".") and expected != base:
        errors.append("package must match source path and app namespace")
    if not is_production(path.parts):
        return errors
    errors += motion_violations(code, builds_tweens=False)
    suffix = package.removeprefix(base).strip(".")
    location = suffix.split(".") if suffix else []
    if "shared" in path.parts and "commonMain" in path.parts:
        if not location or location[0] not in layers:
            errors.append(f"common production code belongs in {'/'.join(sorted(layers))}")
        if uses(code, PLATFORM_APIS):
            errors.append("platform-only API in commonMain")
    if location[:1] == ["designsystem"]:
        errors.append("visual elements, components, and tokens belong in otus-meta")
    if any(part in CATCH_ALL_PACKAGES for part in location):
        errors.append("use responsibility-specific packages instead of catch-all packages")
    if location[:1] == ["feature"] and (len(location) < 3 or location[2] not in FEATURE_LAYERS):
        errors.append("feature code must live in domain/data/presentation")
    errors += reference_violations(location, suffix, code, base)
    if "domain" in location and uses(code, (*PLATFORM_APIS, *DATA_APIS, *UI_APIS)):
        errors.append("domain contains UI/platform/network/database dependencies")
    if ("domain" in location or "data" in location) and uses(code, ("com.otus.designsystem.",)):
        errors.append("domain and data cannot depend on the design system")
    if "presentation" in location and uses(code, DATA_APIS):
        errors.append("UI must not access network or database APIs")
    if ("elements" in location or "components" in location) and re.search(SERVICE_TYPES, code):
        errors.append("elements/components take values and callbacks, not services or navigation")
    if "screens" in location and not path.stem.endswith("Screen"):
        errors.append("screen files must end in Screen.kt")
    if re.search(COMPOSABLE, code):
        allowed = location[:1] == ["app"] or (
            "presentation" in location and any(x in location for x in ("screens", "components"))
        )
        if not allowed:
            errors.append("composables belong in app or presentation screens and components")
    if literal_color(code):
        errors.append("literal colors belong in otus-meta designsystem/theme")
    return sorted(set(errors))


def reference_violations(location: list[str], suffix: str, code: str, base: str) -> list[str]:
    """Dependency directions between app packages, including fully qualified references."""
    errors = []
    for reference in re.findall(r"\b" + re.escape(base) + r"\.([A-Za-z_][\w.]*)", code):
        if reference == suffix:
            continue
        target = reference.split(".")
        if location[:1] == ["core"] and target[0] in {"app", "feature"}:
            errors.append("core cannot depend on app/features")
        if "components" in location and "screens" in target:
            errors.append("components cannot depend on screens")
        if location[:1] not in (["feature"], ["core"]):
            continue
        in_feature = location[:1] == ["feature"]
        if in_feature and target[0] == "app":
            errors.append("features cannot depend on app wiring")
        if in_feature and target[:1] == ["feature"] and target[1:2] != location[1:2]:
            errors.append("features cannot import other features directly")
        # Shared slices of core, such as core/messages, keep the same layers as a feature.
        if "domain" in location and any(x in target for x in ("data", "presentation")):
            errors.append("domain cannot depend on data/presentation")
        if "presentation" in location and "data" in target:
            errors.append("presentation cannot depend on data implementations")
        if "data" in location and "presentation" in target:
            errors.append("data cannot depend on presentation")
    return errors


def drawable_violations(root: Path) -> list[str]:
    """Drawable resources in any source set of the shared or Android module belong in otus-meta."""
    return [
        f"{path.relative_to(root)}: visual drawable resources belong in otus-meta/designsystem"
        for folder in SOURCE_FOLDERS[:2]
        for path in sorted((root / folder).rglob("*"))
        if path.is_file()
        and "build" not in path.relative_to(root).parts
        and DRAWABLE.fullmatch(path.parent.name)
    ]


def app_violations(root: Path, namespace: str, layers: frozenset[str]) -> list[str]:
    """Every error in the app under root, each prefixed with its file."""
    failures = drawable_violations(root)
    for folder in SOURCE_FOLDERS:
        for path in sorted((root / folder).rglob("*")):
            relative = path.relative_to(root)
            if not path.is_file() or "build" in relative.parts:
                continue
            if path.suffix == ".xml" and folder != "iosApp":
                failures += [f"{relative}: {e}" for e in suppression_violations(path.read_text())]
            if path.suffix in {".kt", ".swift"}:
                errors = violations(relative, path.read_text(), namespace, layers)
                failures += [f"{relative}: {error}" for error in errors]
    return failures
