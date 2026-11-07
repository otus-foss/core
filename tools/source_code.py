"""Kotlin source reading and the rules every Otus repository shares.

The checks read source text, not resolved Kotlin types, so aliases hidden behind other APIs still
need review.
"""

import re

PLATFORM_APIS = ("android.", "java.", "javax.", "platform.")
DATA_APIS = ("io.ktor.", "app.cash.sqldelight.", "androidx.room.", "okhttp3.")
UI_APIS = ("androidx.compose.", "androidx.lifecycle.", "org.jetbrains.compose.")
SERVICE_TYPES = (
    r"\b\w*(?:ViewModel|Repository|NavController)\b|\b(?:viewModel|koinInject|hiltViewModel)\s*[(<]"
)
COMPOSABLE = r"@(?:androidx\.compose\.runtime\.)?Composable\b"
# Kotlin, Java, Android, and XML suppressions, also qualified or with a use-site target, such as
# `@kotlin.Suppress`, `@get:Suppress`, or `@[Suppress(...)]`.
SUPPRESSION = re.compile(
    r"@(?:\w+:\s*)?\[?\s*(?:[\w.]+\.)?Suppress(?:Lint|Warnings)?\b"
    r"|ktlint-disable|detekt:|swiftlint:disable|swiftformat:disable|tools:ignore\b"
)
CATCH_ALL_PACKAGES = {"utils", "helpers", "manager"}


def source_code(text: str) -> str:
    """Remove literals and comments before checking code references.

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


def reference_code(code: str) -> str:
    """Separate ranges and join navigation dots without resolving Kotlin types."""
    return re.sub(r"\s*\.\s*", ".", re.sub(r"\.\.<?", " ", code))


def uses(code: str, apis: tuple[str, ...]) -> bool:
    """Whether code names any package in apis, not as part of a longer name."""
    code = reference_code(code)
    return any(re.search(r"(?<![\w.])" + re.escape(api), code) for api in apis)


def suppression_violations(text: str) -> list[str]:
    if SUPPRESSION.search(text):
        return ["inline lint suppression is prohibited; fix the violation"]
    return []


def literal_color(code: str) -> bool:
    """A Color built from numbers, also qualified, through an import alias, or with hsv/hsl."""
    aliases = re.findall(r"^import\s+[\w.]*\bColor\s+as\s+(\w+)", code, re.MULTILINE)
    names = "|".join(["Color", *aliases])
    return bool(
        re.search(
            rf"\b(?:androidx\.compose\.ui\.graphics\.)?(?:{names})"
            r"(?:\s*\(\s*(?:\w+\s*=\s*)?[-+]?\.?\d|\.(?:hsv|hsl)\s*\()",
            code,
        )
    )


def kotlin_header_violations(
    path_parts: tuple[str, ...], code: str
) -> tuple[str | None, list[str]]:
    """The declared package, and errors for a missing package, source folder, or wildcard import."""
    package = re.search(r"^package\s+([\w.]+)", code, re.MULTILINE)
    if not package:
        return None, ["Kotlin file must declare its package"]
    if "kotlin" not in path_parts:
        return None, ["Kotlin source must live under a kotlin source directory"]
    imports = re.findall(r"^import\s+([\w.*]+)", code, re.MULTILINE)
    errors = ["wildcard imports are prohibited"] if any(i.endswith(".*") for i in imports) else []
    return package.group(1), errors


def source_package(path_parts: tuple[str, ...]) -> str:
    """The package a file's path under its kotlin folder stands for."""
    return ".".join(path_parts[path_parts.index("kotlin") + 1 : -1])


def is_production(path_parts: tuple[str, ...]) -> bool:
    return any(part.endswith("Main") or part == "main" for part in path_parts)


FEATURE_LAYERS = frozenset({"domain", "data", "presentation"})
# What code in each layer of a feature may not reach, in the apps and in Meta's feature modules.
LAYER_LIMITS = {
    "domain": (("data", "presentation"), "domain cannot depend on data or presentation"),
    "presentation": (("data",), "presentation cannot depend on data implementations"),
    "data": (("presentation",), "data cannot depend on presentation"),
}


def layer_violations(layer: str | None, targets: list[list[str]]) -> list[str]:
    """Errors for code in a feature's layer that reaches the packages in targets."""
    if layer not in LAYER_LIMITS:
        return []
    forbidden, error = LAYER_LIMITS[layer]
    return [error for target in targets if any(name in target for name in forbidden)]


def screen_name_violations(stem: str) -> list[str]:
    """Errors for a file in a screens package, whose name says it holds a screen."""
    return [] if stem.endswith("Screen") else ["screen files must end in Screen.kt"]


def service_violations(code: str) -> list[str]:
    """Errors for elements and components, which take values and callbacks."""
    if re.search(SERVICE_TYPES, code):
        return ["elements/components take values and callbacks, not services or navigation"]
    return []
