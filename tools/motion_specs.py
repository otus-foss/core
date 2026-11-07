"""Animations take their timing from Meta's Motion: no raw spec and no Compose default spec."""

import re

from source_code import reference_code

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


def motion_violations(code: str, *, builds_tweens: bool) -> list[str]:
    """Animations in code without comments or strings, outside Meta's `theme/motion`.

    A spring or a new curve never passes. Meta's components may build a tween from Motion values
    when [builds_tweens]: it must name its easing, so Compose's default easing cannot slip in, and
    its duration cannot be a number literal. Apps use `motionTween` or `Motion.tween` only.
    """
    code = reference_code(code)
    errors = []
    if re.search(RAW_MOTION, code):
        errors.append("animate through Motion; springs and curves belong in theme/motion")
    for call in re.finditer(TWEEN_CALL, code):
        if not builds_tweens:
            errors.append("animate through Meta's motionTween or Motion.tween, not a raw tween")
            continue
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
