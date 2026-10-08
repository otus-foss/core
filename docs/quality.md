# Quality gate

## Commands

Requires JDK 21 and Python 3.11+. Library modules with Android targets also need the Android SDK.

One-time setup:

```sh
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements-quality.txt
git config --local core.hooksPath .githooks
```

Daily commands:

```sh
./gradlew formatAll
./gradlew checkAll
```

| Command | Scope |
| --- | --- |
| `formatAll` | Spotless/ktlint Kotlin and Gradle formatting, project text whitespace, Ruff fixes/formatting |
| `qualityCheck` | Spotless, detekt, architecture/file-length checks and regression tests, Ruff lint/format |
| `checkAll` | `qualityCheck` plus the `check` task of every library module |

Detekt is skipped while the repository has no library module, because it has no Kotlin source to
read. Kotlin/Native tests for iOS run only on macOS. On Linux, compile the iOS sources of a module
with `./gradlew :<module>:compileKotlinIosSimulatorArm64`; this does not run them.

The CI `quality` workflow runs `qualityCheck` on every pull request and push to `main`. Android and
iOS jobs are added with the first library module. Repository administrators must require the CI
checks on `main` in the branch protection settings; workflow files alone do not enforce them.

## Architecture checks

`tools/check_architecture.py` applies the rules in [the architecture guide](architecture.md) to
every module it finds. It inspects source text, not the Kotlin type graph, so it cannot prove
transitive dependencies or design quality. Module dependencies declared in Gradle and code review
cover the rest. Add a regression test in `tools/tests/` with every rule change.

## File-length limits and exceptions

The hard limit is **300 lines for Kotlin**, including blank lines and comments. Split by
responsibility before reaching it. A rare exception must be the first line of the file:

```kotlin
// otus-file-length: 420 -- Keep this protocol mapping together for review.
```

The reason needs at least 20 characters. Missing or short reasons, duplicate or misplaced
directives, limits at or below the default, and unnecessary exceptions fail. The exception changes
only the size limit. See [decision 0001](decisions/0001-file-length-exceptions.md).

## Commit messages

The versioned `commit-msg` hook rejects invalid titles, missing separators, and bodies outside the
1–8 bullet limit. Review wording and the 72-character title target manually. See
[the commit rules](../AGENTS.md#commit-messages) and [decision 0004](decisions/0004-commit-messages.md).

## Shared tooling

The checkers in `tools/` are the reference copies for all Otus repositories. Each app keeps its
own copy so that a clone builds alone. Change and test the reference copy here first, then update
the apps in the same way.
