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

The versioned Git hooks are the main gate. `pre-commit` runs the Spotless and Ruff checks, and
`pre-push` runs `checkAll` before every push that sends commits, including tags. Both check the
working tree, so stash unrelated changes first. Do not skip them with `--no-verify`.

**CI is paused.** The `quality` workflow runs only when started by hand from GitHub Actions, so
pull requests get no CI results. The local hooks remain the gate. The workflow file says how to
resume.

The CI `quality` workflow runs only on pull requests that change more than Markdown. The `check`
job runs `checkAll`, and the `ios` job runs every module's iOS simulator tests on macOS 15 with
Xcode 26.3. The repositories cannot require checks on their GitHub plan, so CI reports results
but cannot block a merge. The `publish` workflow runs on version tags; see [Releasing](releasing.md)
and [decision 0013](decisions/0013-local-quality-gate.md).

The publisher builds Android and both iOS library variants on Linux and verifies the complete
local Maven publication before uploading. Pull request CI does not build publications. Manual publisher runs perform validation only.
Publication checks cannot replace iOS app linking or simulator tests. The `ios` job is paused
with `if: false`, so CI runs no simulator tests until it is enabled again. See
[decision 0011](decisions/0011-linux-library-publication.md).

## Coverage

Kover measures line and branch coverage. It reports numbers but does not fail any build.

```sh
./gradlew koverHtmlReport
./gradlew koverLog
```

`koverHtmlReport` writes one report for all modules to `build/reports/kover/html/` and one per
module to `<module>/build/reports/kover/html/`. `koverLog` prints line coverage for each module.

Kover counts only the code that runs on the JVM, through the common and Android host tests.
Kotlin/Native tests for iOS do not count, and neither does code in `iosMain`.

Baseline from 2026-10-16, measured on commit `faad586`:

| Module | Line | Branch |
| --- | --- | --- |
| `accounts` | 56.1% | 28.0% |
| `avatar` | 96.8% | 73.3% |
| `dateformat` | 89.8% | 100% |
| `designsystem` | 12.6% | 9.6% |
| `jmap` | 96.4% | 58.5% |
| `push` | 97.3% | 77.3% |
| `settings` | 77.5% | 59.6% |
| All modules | 50.0% | 37.3% |

The `designsystem` module holds two thirds of the measured lines, so it decides most of the
total.

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

## Runners

Linux jobs run on Ubicloud's `ubicloud-standard-2` runners. See [hosting and CI](hosting.md).
