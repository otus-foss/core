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
UI_LAYERS = {"elements", "components", "theme"}
FEATURE_LAYERS = {"domain", "data", "presentation"}
FEATURE_UI_MODULES = frozenset({"accounts", "settings"})
# Design-system code may format dates, but must not reach protocol, storage, or feature modules.
UI_ALLOWED_MODULES = frozenset({"dateformat"})
# Kotlin, Java, and Android suppressions, also when qualified, such as `@kotlin.Suppress`.
SUPPRESSIONS = (
    r"@(?:file:\s*)?(?:[\w.]+\.)?Suppress(?:Lint|Warnings)?\b"
    r"|ktlint-disable|detekt:|swiftlint:disable|swiftformat:disable"
)
SERVICE_TYPES = (
    r"\b\w*(?:ViewModel|Repository|NavController)\b|\b(?:viewModel|koinInject|hiltViewModel)\s*[(<]"
)


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


def module_base(module: str) -> str:
    return f"{NAMESPACE}.{module.replace('-', '.')}"


def is_ui_module(module: str) -> bool:
    return module == "designsystem" or module.startswith("designsystem-")


def uses(code: str, apis: tuple[str, ...]) -> bool:
    return any(re.search(r"(?<![\w.])" + re.escape(api), code) for api in apis)


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
    if package.group(1) != expected or not (expected == base or expected.startswith(base + ".")):
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
    if "theme" not in location and literal_color(code):
        errors.append("literal colors belong in theme")
    if is_ui_module(module):
        errors.extend(ui_violations(location, code, ui_modules | {module}))
    else:
        composable = re.search(r"@(?:androidx\.compose\.runtime\.)?Composable\b", code)
        if module not in FEATURE_UI_MODULES and (uses(code, UI_APIS) or composable):
            errors.append("Compose is limited to design-system, accounts, and settings modules")
        errors.extend(feature_violations(path, base, location, code))
    return sorted(set(errors))


def feature_violations(path: Path, base: str, location: list[str], code: str) -> list[str]:
    """Shared feature modules mirror the apps: domain, data, and presentation (decision 0007)."""
    errors = []
    layer = location[:1]
    if path.parts[0] in FEATURE_UI_MODULES and (not layer or layer[0] not in FEATURE_LAYERS):
        errors.append("shared feature code belongs in domain/data/presentation")
    composable = re.search(r"@(?:androidx\.compose\.runtime\.)?Composable\b", code)
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


def ui_violations(location: list[str], code: str, ui_modules: frozenset[str]) -> list[str]:
    errors = []
    if not location or location[0] not in UI_LAYERS:
        errors.append("design-system code belongs in elements/components/theme")
    if uses(code, DATA_APIS):
        errors.append("UI must not access network or database APIs")
    if location[:1] in (["elements"], ["components"]) and re.search(SERVICE_TYPES, code):
        errors.append("elements/components take values and callbacks, not services or navigation")
    for reference in re.findall(r"\b" + re.escape(NAMESPACE) + r"\.([A-Za-z_][\w.]*)", code):
        target = reference.split(".")
        if location[:1] == ["elements"] and "components" in target:
            errors.append("elements cannot depend on components")
        allowed = ui_modules | UI_ALLOWED_MODULES
        namespace = ".".join(target)
        if not any(
            namespace == m.replace("-", ".") or namespace.startswith(m.replace("-", ".") + ".")
            for m in allowed
        ):
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
