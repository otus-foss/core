"""Fail on explicit source-layout and dependency-policy violations in the shared libraries."""

import re
import sys
from pathlib import Path

from file_length import file_length_violations

ROOT = Path(__file__).resolve().parents[1]
SKIP = {"build", ".gradle", ".git", ".venv"}
NAMESPACE = "com.otus"
PLATFORM_APIS = ("android.", "java.", "javax.", "platform.")
DATA_APIS = ("io.ktor.", "app.cash.sqldelight.", "androidx.room.", "okhttp3.")
UI_APIS = ("androidx.compose.", "androidx.lifecycle.", "org.jetbrains.compose.")
# Libraries are consumed by the apps; they must never reach back into app code.
APP_NAMESPACES = ("com.otus.mail", "com.otus.calendar")
UI_LAYERS = frozenset({"elements", "components"})
UI_SCOPES = frozenset({"shared", "mail", "calendar"})
PACKAGE_SEGMENT = r"[A-Za-z_]\w*"
FEATURE_LAYERS = {"domain", "data", "presentation"}
FEATURE_UI_MODULES = frozenset({"accounts", "settings"})
# Design-system code may format dates, but must not reach protocol, storage, or feature modules.
UI_ALLOWED_MODULES = frozenset({"dateformat"})
# Kotlin, Java, and Android suppressions, also when qualified, such as `@kotlin.Suppress`.
SUPPRESSIONS = (
    r"@(?:file:\s*)?(?:[\w.]+\.)?Suppress(?:Lint|Warnings)?\b"
    r"|ktlint-disable|detekt:|swiftlint:disable|swiftformat:disable"
)
COMPOSABLE = r"@(?:androidx\.compose\.runtime\.)?Composable\b"
SERVICE_TYPES = (
    r"\b\w*(?:ViewModel|Repository|NavController)\b|\b(?:viewModel|koinInject|hiltViewModel)\s*[(<]"
)
# Animation specs that bypass Motion. `Motion.tween` is a member call, so the dot excludes it.
RAW_MOTION = r"(?<![\w.])(?:spring|CubicBezierEasing)\s*[(<]"
TWEEN_CALL = r"(?<![\w.])(?:tween|TweenSpec)\s*(?:<[^<>()]*>)?\s*\("
# Calls that run Compose's own spring, deaf to the user's animation speed, unless given a spec.
DEFAULTED_MOTION = (
    r"(?<!\w)(animateScrollToItem|animateScrollToPage|animateScrollBy|animate\w*AsState)"
    r"\s*(?:<[^<>()]*>)?\s*\("
)
# How many positional arguments come before the spec, where it is not the second.
SPEC_POSITION = {"animateScrollToPage": 2}


def source_code(text: str) -> str:
    """Remove literals/comments before checking code references (not type resolution).

    String templates keep their `${...}` code, so references inside them are still checked.
    Newlines are kept so that line counts do not change.
    """
    output: list[str] = []
    _scan_code(text, 0, output, in_template=False)
    return "".join(output)


def _scan_code(text: str, index: int, output: list[str], in_template: bool) -> int:
    """Copy code from index; inside a template, stop after the brace that closes it."""
    depth = 0
    while index < len(text):
        if text.startswith("//", index):
            end = text.find("\n", index)
            index = len(text) if end < 0 else end
        elif text.startswith("/*", index):
            end = text.find("*/", index + 2)
            end = len(text) if end < 0 else end + 2
            output.append("\n" * text.count("\n", index, end))
            index = end
        elif text[index] == '"':
            index = _scan_string(text, index, output)
        elif text[index] == "'":
            index = _skip_char(text, index)
        else:
            character = text[index]
            index += 1
            if in_template and character == "}" and depth == 0:
                return index
            depth += {"{": 1, "}": -1}.get(character, 0)
            output.append(character)
    return index


def _scan_string(text: str, index: int, output: list[str]) -> int:
    quote = '"""' if text.startswith('"""', index) else '"'
    index += len(quote)
    while index < len(text):
        if text.startswith(quote, index):
            # A raw string may end in extra quotes, such as `""""`.
            index += len(quote)
            while quote == '"""' and text.startswith('"', index):
                index += 1
            return index
        if quote == '"' and text[index] == "\\":
            index += 2
        elif text.startswith("${", index):
            output.append(" ")
            index = _scan_code(text, index + 2, output, in_template=True)
            output.append(" ")
        else:
            if text[index] == "\n":
                output.append("\n")
            index += 1
    return index


def _skip_char(text: str, index: int) -> int:
    index += 1
    while index < len(text) and text[index] != "'":
        index += 2 if text[index] == "\\" else 1
    return index + 1


def literal_color(code: str) -> bool:
    """A Color call with a number, also through an import alias such as `Color as Tint`."""
    aliases = re.findall(r"^import\s+[\w.]*\bColor\s+as\s+(\w+)", code, re.MULTILINE)
    names = "|".join(["Color", *aliases])
    return bool(re.search(rf"\b(?:{names})\s*\(\s*(?:\w+\s*=\s*)?[-+]?\.?\d", code))


def module_namespace(module: str) -> str:
    """The package suffix of a module: its directory name with `-` as `.`."""
    return module.replace("-", ".")


def module_base(module: str) -> str:
    return f"{NAMESPACE}.{module_namespace(module)}"


def is_ui_module(module: str) -> bool:
    return module == "designsystem" or module.startswith("designsystem-")


def reference_code(code: str) -> str:
    """Separate ranges and join navigation dots without resolving Kotlin types."""
    return re.sub(r"\s*\.\s*", ".", re.sub(r"\.\.<?", " ", code))


def uses(code: str, apis: tuple[str, ...]) -> bool:
    code = reference_code(code)
    return any(re.search(r"(?<![\w.])" + re.escape(api), code) for api in apis)


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
    errors = file_length_violations(path, text)
    if re.search(SUPPRESSIONS, text):
        errors.append("inline lint suppression is prohibited; fix the violation")
    if path.suffix != ".kt":
        return errors
    module = path.parts[0]
    base = module_base(module)
    code = source_code(text)
    package = re.search(r"^package\s+([\w.]+)", code, re.MULTILINE)
    if not package:
        return [*errors, "Kotlin file must declare its package"]
    if "kotlin" not in path.parts:
        return [*errors, "Kotlin source must live under a kotlin source directory"]
    expected = ".".join(path.parts[path.parts.index("kotlin") + 1 : -1])
    package_matches = package.group(1) == expected and (
        expected == base or expected.startswith(base + ".")
    )
    if not package_matches:
        errors.append(f"package must match source path and the module namespace {base}")
    imports = re.findall(r"^import\s+([\w.*]+)", code, re.MULTILINE)
    if any(name.endswith(".*") for name in imports):
        errors.append("wildcard imports are prohibited")
    if uses(code, APP_NAMESPACES):
        errors.append("shared libraries cannot depend on Mail or Calendar app code")
    if not any(part.endswith("Main") or part == "main" for part in path.parts):
        return sorted(set(errors))
    location = expected.removeprefix(base).strip(".").split(".") if expected != base else []
    if "commonMain" in path.parts and uses(code, PLATFORM_APIS):
        errors.append("platform-only API in commonMain")
    if any(part in {"utils", "helpers", "manager"} for part in location):
        errors.append("use responsibility-specific packages instead of catch-all packages")
    if "domain" in location and uses(code, (*PLATFORM_APIS, *DATA_APIS, *UI_APIS)):
        errors.append("domain contains UI/platform/network/database dependencies")
    in_theme = package_matches and is_ui_module(module) and theme_location(location)
    if not in_theme and literal_color(code):
        errors.append("literal colors belong in theme")
    if not (in_theme and location[:2] == ["theme", "motion"]):
        errors.extend(motion_violations(code))
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
            errors.append("Compose is limited to design-system, accounts, and settings modules")
        errors.extend(feature_violations(path, base, location, code))
    return sorted(set(errors))


def motion_violations(code: str) -> list[str]:
    """Animations outside `theme/motion` take their duration and curve from Motion.

    A spring or a new curve never does. A tween must name its easing, so Compose's default easing
    cannot slip in, and its duration cannot be a number literal.
    """
    code = reference_code(code)
    errors = []
    if re.search(RAW_MOTION, code):
        errors.append("animate through Motion; springs and curves belong in theme/motion")
    for call in re.finditer(TWEEN_CALL, code):
        arguments = call_arguments(code, call.end() - 1)
        if not re.search(r"\beasing\s*=", arguments):
            errors.append("a tween outside theme/motion must name a MotionCurve easing")
        if re.match(r"\s*(?:durationMillis\s*=\s*)?[-+]?\d", arguments):
            errors.append("tween durations come from Motion, not number literals")
    for call in re.finditer(DEFAULTED_MOTION, code):
        name = call.group(1)
        arguments = top_level_arguments(call_arguments(code, call.end() - 1))
        named = [argument for argument in arguments if re.match(r"\s*\w+\s*=(?!=)", argument)]
        has_spec = any(re.match(r"\s*animationSpec\s*=", argument) for argument in named)
        if not has_spec and len(arguments) - len(named) <= SPEC_POSITION.get(name, 1):
            errors.append(f"give {name} an animationSpec from Motion")
    return errors


def top_level_arguments(arguments: str) -> list[str]:
    """The comma-separated arguments of a call, leaving commas inside nested brackets alone."""
    parts, depth, start = [], 0, 0
    for index, char in enumerate(arguments):
        depth += {"(": 1, "[": 1, "{": 1, ")": -1, "]": -1, "}": -1}.get(char, 0)
        if char == "," and depth == 0:
            parts.append(arguments[start:index])
            start = index + 1
    parts.append(arguments[start:])
    return [part for part in parts if part.strip()]


def call_arguments(code: str, open_paren: int) -> str:
    """The text between the parenthesis at open_paren and the one that closes it."""
    depth = 0
    for index in range(open_paren, len(code)):
        depth += {"(": 1, ")": -1}.get(code[index], 0)
        if depth == 0:
            return code[open_paren + 1 : index]
    return code[open_paren + 1 :]


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
    other_features = tuple(f"com.otus.{name}." for name in FEATURE_UI_MODULES - {module})
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


def modules() -> list[str]:
    return sorted(
        path.parent.name
        for path in ROOT.glob("*/build.gradle.kts")
        if (path.parent / "src").is_dir() and path.parent.name not in SKIP
    )


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


def main() -> int:
    names = modules()
    ui_modules = frozenset(name for name in names if is_ui_module(name))
    failures = []
    for module in names:
        for path in sorted((ROOT / module / "src").rglob("*")):
            if not path.is_file() or set(path.parts) & SKIP:
                continue
            relative = path.relative_to(ROOT)
            for error in visual_resource_violations(relative):
                failures.append(f"{relative}: {error}")
            if path.suffix not in {".kt", ".swift"}:
                continue
            for error in violations(relative, path.read_text(), ui_modules):
                failures.append(f"{relative}: {error}")
    if failures:
        print("\n".join(failures), file=sys.stderr)
        return 1
    print(f"Architecture checks passed for {len(names)} module(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
