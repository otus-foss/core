"""Secrets reach only the workflow steps that need them, never a Gradle build."""

import re
from pathlib import Path

# `secrets.NAME` and `secrets[expression]`.
REPOSITORY_SECRET = re.compile(r"\bsecrets\s*[.\[]")
# A Gradle build runs code from dependencies, so it gets neither a secret nor the job's token.
GRADLE_SECRET = re.compile(REPOSITORY_SECRET.pattern + r"|\bgithub\.token\b")
STEP = re.compile(r" {6}- ")
SHARED_ENV = re.compile(r"(?: {4})?env:\s*$")


def steps(text: str) -> list[str]:
    """Each step's text, from its `- ` line to the next step or the end of its job."""
    found: list[list[str]] = []
    current: list[str] | None = None
    for line in text.splitlines():
        if STEP.match(line):
            current = [line]
            found.append(current)
        elif current is not None and (line.startswith(" " * 8) or not line.strip()):
            current.append(line)
        else:
            current = None
    return ["\n".join(step) for step in found]


def shared_env(text: str) -> list[str]:
    """The `env` blocks of whole workflows and jobs, which every step of theirs receives."""
    blocks: list[str] = []
    lines = text.splitlines()
    for i, line in enumerate(lines):
        if not SHARED_ENV.fullmatch(line):
            continue
        indent = len(line) - len(line.lstrip())
        block = []
        for following in lines[i + 1 :]:
            if following.strip() and len(following) - len(following.lstrip()) <= indent:
                break
            block.append(following)
        blocks.append("\n".join(block))
    return blocks


def step_name(step: str) -> str:
    match = re.search(r"name: (.+)", step)
    return match.group(1).strip() if match else ""


def workflow_secret_violations(text: str) -> list[str]:
    errors = [
        f"step '{step_name(step) or step.splitlines()[0].strip()}' runs Gradle with a secret"
        for step in steps(text)
        if "gradlew" in step and GRADLE_SECRET.search(step)
    ]
    if any(REPOSITORY_SECRET.search(block) for block in shared_env(text)):
        errors.append("a workflow or job env shares a secret with every step; give it to one step")
    return errors


def workflow_violations(root: Path) -> list[str]:
    return [
        f"{path.relative_to(root)}: {error}"
        for path in sorted((root / ".github/workflows").glob("*.yml"))
        for error in workflow_secret_violations(path.read_text())
    ]
