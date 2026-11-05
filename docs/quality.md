# Quality gate

## Commands

Requires JDK 21 and Python 3.11+. Library modules with Android targets also need the Android SDK.

One-time setup:

```sh
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements-quality.txt
git config --local core.hooksPath .githooks
```

Gradle uses the repository's Python virtual environment when present. A linked worktree without
one uses the main checkout's `.venv`.

Test code gets ktlint formatting only. detekt and Android lint (`ignoreTestSources`) skip test
source sets. The apps' lint analyzes Meta modules through `checkDependencies`, so this keeps their
`checkAll` fast too.

During development, batch edits and run the linters plus the test classes affected by the change:

```sh
./gradlew spotlessCheck detekt ruffCheck ruffFormatCheck
./gradlew :dateformat:testAndroidHostTest --tests 'com.otus.dateformat.DateFormatterTest'
```

Use `qualityCheck` when architecture and tooling checks are also needed. Run `formatAll` when
files need formatting. Run `checkAll` once after the final batch, and repeat it only when later
changes or failures require another full check.

| Command | Scope |
| --- | --- |
| `formatAll` | Spotless/ktlint Kotlin and Gradle formatting, project text whitespace, Ruff fixes/formatting |
| `qualityCheck` | Spotless, detekt, architecture/file-length checks and regression tests, Ruff lint/format |
| `checkAll` | `qualityCheck` plus the `check` task of every library module |

Use `--profile` to write task timings to `build/reports/profile/` and `--info` to explain cache
misses. For actual test timings, add `--rerun` to a specific test task, such as
`./gradlew :designsystem:testAndroidHostTest --rerun --profile`. This reruns that test task without
forcing its compilation dependencies to rerun. Record shared-machine queue waits separately;
Gradle includes them in task time. Test time includes Robolectric and native-renderer startup.

Screenshot baselines use relative path sensitivity so the checkout's absolute path does not
change their input fingerprints. Keep baseline names and contents as task inputs so an edited
PNG still invalidates verification.
Recording with `-Proborazzi.record` always executes and does not cache test results, because
baseline PNGs are written outside the test task's declared output directories.

Detekt is skipped while the repository has no library module, because it has no Kotlin source to
read. Kotlin/Native tests for iOS run only on macOS. On Linux, compile the iOS sources of a module
with `./gradlew :<module>:compileKotlinIosSimulatorArm64`; this does not run them.

The versioned Git hooks are the main gate. `pre-commit` runs the Spotless and Ruff checks on the
working tree, so stash unrelated changes first. `commit-msg` checks the message and removes AI
attribution lines, such as `Co-authored-by` trailers that name Claude, Anthropic, or Cursor. No
hook runs `checkAll`, because a full build on every push is too heavy for a laptop. Run
`./gradlew checkAll` before pushing work that needs it, such as a direct push to `main` or a
release tag. Do not skip the hooks with `--no-verify`.

**CI runs once per pull request,** on Ubicloud runners. The `quality` workflow starts when a pull
request that is not a draft opens, or when a draft is marked ready for review. Later pushes do not
start it again, so open pull requests as drafts and mark them ready once every commit is pushed.
Start it by hand from GitHub Actions to check again. Every push to `main`, which includes each
merged pull request, runs it again to check `main` itself, and those runs never cancel each other.
The macOS `ios` job runs only while the repository variable `IOS_CI` is `on`. Leave it unset to
save macOS minutes; set it to `on` under Settings › Secrets and variables › Actions to turn it back on.

The CI `quality` workflow runs only on pull requests that change more than Markdown. The `check`
job runs `checkAll` as three parallel jobs: `qualityCheck`, `checkDesignSystem`, and
`checkLibraries`. The `ios` job runs every module's iOS simulator tests on GitHub's `macos-15`
runners with Xcode 26.3, because Ubicloud has no macOS runners. The
repositories cannot require checks on their GitHub plan, so CI reports results but cannot block a
merge. The `publish` workflow is paused too: it runs only by hand and then validates without
uploading, and a version tag starts nothing. See [Releasing](releasing.md) and
[decision 0013](decisions/0013-local-quality-gate.md).

The publisher builds Android and both iOS library variants on Linux and verifies the complete
local Maven publication before uploading. Pull request CI does not build publications. Manual publisher runs perform validation only.
Publication checks cannot replace iOS app linking or simulator tests, which only the `ios` job
runs. See [decision 0011](decisions/0011-linux-library-publication.md).

## Coverage

Kover measures line and branch coverage. It reports numbers but does not fail any build.
Tests run without coverage instrumentation unless the Gradle command names a Kover task, so
`checkAll` does not pay for it.

```sh
./gradlew koverHtmlReport
./gradlew koverLog
```

`koverHtmlReport` writes one report for all modules to `build/reports/kover/html/` and one per
module to `<module>/build/reports/kover/html/`. `koverLog` prints line coverage for each module.

Kover counts only the code that runs on the JVM, through the common and Android host tests.
Kotlin/Native tests for iOS do not count, and neither does code in `iosMain`.

Measured on 2026-10-16, after the design system screenshot tests. The `jmap-push` module was
named `push` at the time:

| Module | Line | Branch |
| --- | --- | --- |
| `accounts` | 56.1% | 28.0% |
| `avatar` | 96.8% | 73.3% |
| `dateformat` | 89.8% | 100% |
| `designsystem` | 92.1% | 56.8% |
| `jmap` | 96.4% | 58.5% |
| `jmap-push` | 97.3% | 77.3% |
| `settings` | 77.5% | 59.6% |
| All modules | 90.6% | 60.6% |

The `designsystem` module holds two thirds of the measured lines, so it decides most of the
total. Its screenshot tests raised it from 12.6% of lines; see
[screenshot tests](designsystem.md#screenshot-tests).

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
