"""Source-layout and dependency rules for Meta's library modules; see docs/architecture.md."""

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
    reference_code,
    source_code,
    source_package,
    suppression_violations,
    uses,
)

NAMESPACE = "com.otus"
# Libraries are consumed by the apps; they must never reach back into app code.
APP_NAMESPACES = ("com.otus.mail", "com.otus.calendar")
UI_LAYERS = frozenset({"elements", "components"})
UI_SCOPES = frozenset({"shared", "mail", "calendar"})
PACKAGE_SEGMENT = r"[A-Za-z_]\w*"
FEATURE_LAYERS = {"domain", "data", "presentation"}
FEATURE_UI_MODULES = frozenset({"accounts", "avatar-ui", "settings"})
# Design-system code may format dates, but must not reach protocol, storage, or feature modules.
UI_ALLOWED_MODULES = frozenset({"dateformat"})


def module_namespace(module: str) -> str:
    """The package suffix of a module: its directory name with `-` as `.`."""
    return module.replace("-", ".")


def module_base(module: str) -> str:
    return f"{NAMESPACE}.{module_namespace(module)}"


def is_ui_module(module: str) -> bool:
    return module == "designsystem" or module.startswith("designsystem-")


def scoped_ui_location(location: list[str]) -> bool:
    return (
        len(location) == 3
        and location[0] in UI_SCOPES
        and bool(re.fullmatch(PACKAGE_SEGMENT, location[1]))
        and location[1] not in UI_LAYERS | {"theme", "resources"}
        and location[2] in UI_LAYERS
    )


def ui_layer(location: list[str]) -> list[str]:
    """`["elements"]` or `["components"]` for scoped UI code, else empty."""
    return location[2:3] if scoped_ui_location(location) else []


def theme_location(location: list[str]) -> bool:
    return location[:1] == ["theme"] and all(
        re.fullmatch(PACKAGE_SEGMENT, part) for part in location[1:]
    )


def library_references(code: str) -> list[str]:
    # A package declaration describes this file, not a dependency.
    code = re.sub(r"^package\s+[\w.]+[ \t]*;?", "", code, flags=re.MULTILINE)
    # Keep import spelling intact so malformed package segments still fail validation.
    imports = re.compile(r"\bimport\s+[\w.*]+")
    code = "\n".join([*imports.findall(code), reference_code(imports.sub("", code))])
    return re.findall(r"(?<![\w.])" + re.escape(NAMESPACE) + r"\.([A-Za-z_][\w.]*)", code)


def ui_reference_violations(
    location: list[str], references: list[str], ui_modules: frozenset[str]
) -> list[str]:
    errors = []
    scope = location[0] if location else None
    layer = ui_layer(location)
    # Split modules have longer namespaces, such as designsystem.controls.
    namespaces = sorted(
        (module_namespace(name) for name in ui_modules | {"designsystem"}),
        key=len,
        reverse=True,
    )
    for reference in references:
        namespace = next((name for name in namespaces if reference.startswith(name + ".")), None)
        if namespace is None:
            continue
        target = reference.removeprefix(namespace + ".").split(".")
        target_scope = target[0]
        if (
            not (len(target) > 1 and theme_location(target))
            and target_scope != "resources"
            and (len(target) < 4 or not scoped_ui_location(target[:3]))
        ):
            errors.append(
                "design-system references must use <scope>.<group>.elements/components "
                "or theme/resources"
            )
        if layer == ["elements"] and scoped_ui_location(target[:3]) and target[2] == "components":
            errors.append("elements cannot depend on components")
        if scope == "shared" and target_scope in {"mail", "calendar"}:
            errors.append("shared UI cannot depend on mail or calendar UI")
        if scope in {"mail", "calendar"} and target_scope in {"mail", "calendar"} - {scope}:
            errors.append("mail and calendar UI cannot depend on each other")
        if scope == "theme" and target_scope in UI_SCOPES:
            errors.append("theme cannot depend on scoped UI")
    return errors


def violations(path: Path, text: str, ui_modules: frozenset[str] = frozenset()) -> list[str]:
    """Errors for one file of a library module; path starts with the module directory."""
    errors = file_length_violations(path, text) + suppression_violations(text)
    if path.suffix != ".kt":
        return errors
    module = path.parts[0]
    base = module_base(module)
    code = source_code(text)
    package, header_errors = kotlin_header_violations(path.parts, code)
    if package is None:
        return [*errors, *header_errors]
    errors += header_errors
    expected = source_package(path.parts)
    package_matches = package == expected and (expected == base or expected.startswith(base + "."))
    if not package_matches:
        errors.append(f"package must match source path and the module namespace {base}")
    if uses(code, APP_NAMESPACES):
        errors.append("shared libraries cannot depend on Mail or Calendar app code")
    if not is_production(path.parts):
        return sorted(set(errors))
    location = expected.removeprefix(base).strip(".").split(".") if expected != base else []
    if "commonMain" in path.parts and uses(code, PLATFORM_APIS):
        errors.append("platform-only API in commonMain")
    if any(part in CATCH_ALL_PACKAGES for part in location):
        errors.append("use responsibility-specific packages instead of catch-all packages")
    if "domain" in location and uses(code, (*PLATFORM_APIS, *DATA_APIS, *UI_APIS)):
        errors.append("domain contains UI/platform/network/database dependencies")
    in_theme = package_matches and is_ui_module(module) and theme_location(location)
    if not in_theme and literal_color(code):
        errors.append("literal colors belong in theme")
    if not (in_theme and location[:2] == ["theme", "motion"]):
        errors.extend(motion_violations(code, builds_tweens=True))
    references = library_references(code)
    if is_ui_module(module):
        ui_modules = ui_modules | {module}
    errors.extend(
        ui_reference_violations(location if is_ui_module(module) else [], references, ui_modules)
    )
    if is_ui_module(module):
        errors.extend(ui_violations(location, code, references, ui_modules))
    else:
        composable = re.search(COMPOSABLE, code)
        if module not in FEATURE_UI_MODULES and (uses(code, UI_APIS) or composable):
            errors.append(
                "Compose is limited to design-system, accounts, avatar-ui, and settings modules"
            )
        errors.extend(feature_violations(path, base, location, code))
    return sorted(set(errors))


def feature_violations(path: Path, base: str, location: list[str], code: str) -> list[str]:
    """Shared feature modules mirror the apps: domain, data, and presentation (decision 0007)."""
    errors = []
    layer = location[:1]
    if path.parts[0] in FEATURE_UI_MODULES and (not layer or layer[0] not in FEATURE_LAYERS):
        errors.append("shared feature code belongs in domain/data/presentation")
    composable = re.search(COMPOSABLE, code)
    if (uses(code, UI_APIS) or composable) and layer != ["presentation"]:
        errors.append("Compose belongs in design-system modules or a feature's presentation layer")
    if (
        composable
        and layer == ["presentation"]
        and location[1:2] not in (["components"], ["screens"])
    ):
        errors.append("composables belong in presentation/components or presentation/screens")
    if (
        location[1:2] == ["screens"]
        and layer == ["presentation"]
        and not path.stem.endswith("Screen")
    ):
        errors.append("screen files must end in Screen.kt")
    if location[1:2] == ["components"] and re.search(SERVICE_TYPES, code):
        errors.append("elements/components take values and callbacks, not services or navigation")
    if layer == ["presentation"] and uses(code, DATA_APIS):
        errors.append("UI must not access network or database APIs")
    if layer == ["domain"] and uses(code, ("com.otus.designsystem.",)):
        errors.append("domain cannot depend on the design system")
    if layer == ["presentation"] and uses(code, ("com.otus.jmap.",)):
        errors.append("presentation uses domain contracts, not protocol clients")
    module = path.parts[0]
    other_features = tuple(
        f"com.otus.{module_namespace(name)}." for name in FEATURE_UI_MODULES - {module}
    )
    if uses(code, other_features):
        errors.append("shared features cannot depend directly on another shared feature")
    for reference in re.findall(r"\b" + re.escape(base) + r"\.([A-Za-z_][\w.]*)", code):
        target = reference.split(".")[:1]
        if layer == ["domain"] and target in (["data"], ["presentation"]):
            errors.append("domain cannot depend on data or presentation")
        if layer == ["presentation"] and target == ["data"]:
            errors.append("presentation cannot depend on data implementations")
        if layer == ["data"] and target == ["presentation"]:
            errors.append("data cannot depend on presentation")
    return errors


def ui_violations(
    location: list[str], code: str, references: list[str], ui_modules: frozenset[str]
) -> list[str]:
    errors = []
    if not theme_location(location) and not scoped_ui_location(location):
        errors.append(
            "design-system code belongs in <shared|mail|calendar>/<group>/elements|components "
            "or root theme and its subpackages"
        )
    if uses(code, DATA_APIS):
        errors.append("UI must not access network or database APIs")
    layer = ui_layer(location)
    if layer in (["elements"], ["components"]) and re.search(SERVICE_TYPES, code):
        errors.append("elements/components take values and callbacks, not services or navigation")
    allowed = [module_namespace(module) for module in ui_modules | UI_ALLOWED_MODULES]
    for namespace in references:
        if not any(namespace == name or namespace.startswith(name + ".") for name in allowed):
            errors.append("design-system modules may depend only on design-system and dateformat")
    return errors


def visual_resource_violations(path: Path) -> list[str]:
    # Qualified directories such as drawable-night or drawable-xxhdpi hold drawables too.
    drawable = any(part == "drawable" or part.startswith("drawable-") for part in path.parts)
    if (
        ("composeResources" in path.parts or "res" in path.parts)
        and drawable
        and not is_ui_module(path.parts[0])
    ):
        return ["visual drawable resources belong in a design-system module"]
    return []


def library_violations(root: Path, modules: list[str]) -> list[str]:
    """Every error in the library modules under root, each prefixed with its file."""
    ui_modules = frozenset(name for name in modules if is_ui_module(name))
    failures = []
    for module in modules:
        for path in sorted((root / module / "src").rglob("*")):
            if not path.is_file() or "build" in path.relative_to(root).parts:
                continue
            relative = path.relative_to(root)
            failures += [f"{relative}: {error}" for error in visual_resource_violations(relative)]
            if path.suffix == ".xml":
                failures += [f"{relative}: {e}" for e in suppression_violations(path.read_text())]
            if path.suffix in {".kt", ".swift"}:
                errors = violations(relative, path.read_text(), ui_modules)
                failures += [f"{relative}: {error}" for error in errors]
    return failures
