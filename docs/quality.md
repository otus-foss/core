# Quality gate

This guide covers all three repositories: the commands, the shared build logic and tools, and
the rules they enforce. Each app's `docs/quality.md` adds only what is particular to it, such as
its iOS and screenshot tests.

## Commands

Requires JDK 21 and Python 3.11+, plus the Android SDK for Android checks. No Node.js or npm
installation is required; see [decision 0002](decisions/0002-gradle-quality-entry-points.md). The
apps also need an otus-meta checkout beside them; see [consuming](consuming.md).

One-time setup in each repository:

```sh
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements-quality.txt
git config --local core.hooksPath .githooks
```

Gradle uses the repository's Python virtual environment when present. A linked worktree without
one uses the main checkout's `.venv`.

During development, batch edits and run the linters plus the test classes affected by the change:

```sh
./gradlew spotlessCheck detekt ruffCheck ruffFormatCheck
./gradlew :dateformat:testAndroidHostTest --tests 'com.otus.dateformat.DateFormatterTest'
```

Use `qualityCheck` when architecture and tooling checks are also needed. Run `formatAll` when
files need formatting; it cannot fix every static-analysis violation. Run `checkAll` once after
the final batch, and repeat it only when later changes or failures require another full check.

| Command | Scope |
| --- | --- |
| `formatAll` | Spotless/ktlint Kotlin and Gradle formatting, project text whitespace, Ruff fixes/formatting |
| `qualityCheck` | Spotless, detekt, architecture checks, the repository's tool tests, Ruff lint/format |
| `checkAll` | In Meta, `qualityCheck` plus the `check` task of every library module; in the apps, see their guides |

Project text formatting covers Markdown, YAML, JSON, XML/plist, and TOML with trailing-whitespace
removal and a final newline. It does not enforce full syntax-specific formatting or Markdown
structure. Ruff checks Python files only.

Test code gets ktlint formatting only. detekt and Android lint (`ignoreTestSources`) skip test
source sets, because analyzing them took about a third of lint's time and found little. The apps'
lint analyzes Meta modules through `checkDependencies`, so this keeps their `checkAll` fast too.

Kotlin/Native tests for iOS run only on macOS. On Linux, compile the iOS sources of a module with
`./gradlew :<module>:compileKotlinIosSimulatorArm64`; this does not run them.

## Build logic

`build-logic/` is an included build of convention plugins. Meta includes it from its settings
file; the apps include it from their otus-meta checkout through `pluginManagement`. Every
version they read comes from `gradle/libs.versions.toml`.

| Plugin | Applied to | What it sets up |
| --- | --- | --- |
| `otus.settings` | every settings file | the Google and Maven Central repositories |
| `otus.quality` | every root project | Spotless, detekt with Meta's `config/detekt.yml`, the architecture check and tool tests, Ruff, `formatAll`, `qualityCheck`, Kover, test sharding |
| `otus.kmp-library` | Meta's modules without UI | iOS and Android targets, SDKs, JVM 11, warnings as errors, explicit API, the package `com.otus.<module>`, publishing |
| `otus.kmp-compose-library` | Meta's Compose modules | the same, with Compose and Android resources |
| `otus.kmp-app` | an app's `shared` module | the same targets and SDKs, Compose, and the static `Shared` iOS framework |
| `otus.android-app` | an app's `androidApp` module | namespace and application id `com.otus.<app>`, SDKs, JVM 11, lint, warnings as errors |
| `otus.screenshots` | modules with Roborazzi tests | the baseline folder as a test input, and `-Proborazzi.record` |

Each repository's root build file adds only its own `checkAll` parts, which CI runs as parallel
jobs, and tasks such as Mail's `pushBridgeTest`. Every module's `check` depends on the root
`qualityCheck`, so CI jobs other than the quality job pass `-x qualityCheck` where they run
`check`.

`architectureTest` runs the Python tests in the repository's own `tools/tests`, with Meta's
`tools` on `PYTHONPATH`; a repository without such tests, such as Calendar, has no
`architectureTest`.

Use `--profile` to write task timings to `build/reports/profile/` and `--info` to explain cache
misses. For actual test timings, add `--rerun` to a specific test task, such as
`./gradlew :designsystem:testAndroidHostTest --rerun --profile`. This reruns that test task without
forcing its compilation dependencies to rerun. Record shared-machine queue waits separately;
Gradle includes them in task time. Test time includes Robolectric and native-renderer startup.

Screenshot baselines use relative path sensitivity so the checkout's absolute path does not
change their input fingerprints. Keep baseline names and contents as task inputs so an edited
PNG still invalidates verification. Recording with `-Proborazzi.record` always executes and does
not cache test results, because baseline PNGs are written outside the test task's declared output
directories.

## Shared tools and configuration

Every repository runs the tools in Meta's `tools/` from its otus-meta checkout, so a rule changes
in one place. Their tests live in `tools/tests` here.

| Tool | Purpose |
| --- | --- |
| `check_architecture.py` | source layout, dependency rules, and the shared files below |
| `check_commit_message.py` | the `commit-msg` hook of every repository |
| `coverage_report.py` | the coverage summary |
| `launch_colors.py` | writes an app's launch-window colors from the theme |
| `brand_drawables.py`, `check_publications.py` | Meta's brand drawables and release check |

One copy of `config/detekt.yml` serves all three repositories. `.editorconfig` and `ruff.toml`
must sit at each repository's root for editors and Ruff to find them, so the apps keep copies,
and the architecture check fails when a copy differs from Meta's.

## Architecture checks

`tools/check_architecture.py` runs from a repository's root and reads its `config/checks.toml`:

```toml
namespace = "com.otus.mail"
kind = "app"
layers = ["app", "core", "feature"]
```

A `library` repository's modules are the ones its settings file includes; their rules follow
[the architecture guide](architecture.md). An `app` follows its own `docs/architecture.md`: its
shared common code lives in the listed layers, features never import each other, and every
visual element, component, token, and drawable comes from Meta. Both kinds share these rules:

- package names match the source path and the namespace, and no wildcard imports;
- no inline suppressions, in Kotlin, Swift, or Android XML, such as `@Suppress`, `@get:Suppress`,
  `@SuppressLint`, or `tools:ignore`;
- no literal colors outside Meta's theme, also through an import alias, a qualified name, or
  `Color.hsv`;
- animations take their timing from `Motion`: no spring or new curve, and no Compose call left on
  its default spring. Meta's components may build a `tween` with a named easing and a duration
  from `Motion`; apps use `motionTween` or `Motion.tween` only;
- the file-length limits below;
- no repository secret reaches a Gradle step or a shared workflow `env`.

An app also fails when `androidApp/src/main/res/values/colors.xml` differs from what
`launch_colors.py` writes from the Otus theme, so the launch window always matches the first
Compose frame. Run `python3 ../otus-meta/tools/launch_colors.py` from the app's root after a
theme change.

The checker inspects source text, not the Kotlin type graph, so it cannot prove transitive
dependencies or design quality. Module dependencies declared in Gradle and code review cover the
rest. Add a regression test in `tools/tests/` with every rule change.

## File-length limits and exceptions

Aim for 100–200 lines for most screens and components. The hard limits are **300 lines for
Kotlin** and **200 lines for Swift**, including blank lines and comments. These apply to
handwritten source and tests, not Gradle scripts, generated build output, assets, or
documentation. Split by responsibility before reaching the limit; do not compress code or remove
useful comments to satisfy it.

A rare exception must be the first line of the file, with an explicit higher limit and a concrete
reason of at least 20 characters:

```kotlin
// otus-file-length: 420 -- Keep this protocol mapping together for review.
```

This permits at most 420 total lines in that file. It does not disable any other check. Missing
or short reasons, duplicate or misplaced directives, limits at or below the default, and
unnecessary exceptions fail. Do not increase the limit routinely as a file grows. The apps disable
SwiftLint's overlapping `file_length` rule, so the same exception works for Swift. See
[decision 0001](decisions/0001-file-length-exceptions.md).

## Enforcement

No lint baselines, blanket warning allowances, or failure-tolerant checks. The narrowly scoped
file-length exception above is permitted. Kotlin source warnings are errors; warnings from
third-party Gradle plugins may still occur.

The versioned Git hooks are the main gate. `pre-commit` runs the Spotless and Ruff checks on the
working tree, so stash unrelated changes first. `commit-msg` checks the message. No hook runs
`checkAll`, because a full build on every push is too heavy for a laptop. Run `./gradlew checkAll`
before pushing work that needs it, such as a direct push to `main` or a release tag. Do not skip
the hooks with `--no-verify`.

**CI runs once per pull request,** on Ubicloud runners, when a pull request that is not a draft
opens or a draft is marked ready; see [hosting and CI](hosting.md). The repositories cannot
require checks on their GitHub plan, so CI reports results but cannot block a merge. See
[decision 0013](decisions/0013-local-quality-gate.md).

In Meta, the `check` job runs `checkAll` as three parallel jobs: `qualityCheck`,
`checkDesignSystem`, and `checkLibraries`. The `ios` job runs every module's iOS simulator tests.
The `publish` workflow is paused: it runs only by hand and then validates without uploading. The
publisher builds Android and both iOS library variants on Linux and verifies the complete local
Maven publication; pull request CI does not build publications. See [Releasing](releasing.md) and
[decision 0011](decisions/0011-linux-library-publication.md).

## Commit messages

The versioned `commit-msg` hook runs `tools/check_commit_message.py`; the apps' hooks run the copy
in their otus-meta checkout. It rejects invalid title syntax, missing blank separators, and bodies
outside the 1–8 bullet limit. Optional footers follow a blank line after the bullets. Footers
begin with a token such as `Refs: #123`, `Closes #123`, or `BREAKING CHANGE: ...`; multiline footer
text is allowed. Wrapped bullet lines start with at least two spaces. The checker ignores editor
comments according to Git's configured comment character. It only checks: it never edits the
message, so attribution lines stay as written.

Review wording and the 72-character title target manually. Git-generated merge messages are
exempt during a merge. Temporary `fixup!`, `squash!`, and `amend!` messages are exempt; review the
final squash message before committing it. The checker's tests run here, including Git rejection
in a temporary repository. See [the commit rules](../AGENTS.md#commit-messages) and
[decision 0004](decisions/0004-commit-messages.md).

## Coverage

Kover measures line and branch coverage. It reports numbers but does not fail any build.
Tests run without coverage instrumentation unless the Gradle command names a Kover task, so
`checkAll` does not pay for it.

```sh
./gradlew koverXmlReport koverHtmlReport
./gradlew koverLog
python3 tools/coverage_report.py build/reports/kover/report.xml --base origin/main
```

`koverHtmlReport` writes one report for all modules to `build/reports/kover/html/` and one per
module to `<module>/build/reports/kover/html/`; `koverXmlReport` writes
`build/reports/kover/report.xml`. `koverLog` prints line coverage for each module. The apps use a
`coverage` variant that runs each test suite once; see their guides.

Kover counts only the code that runs on the JVM, through the common and Android host tests and an
app's unit tests. Kotlin/Native tests for iOS do not count, and neither does code in `iosMain`.

The `coverage` workflow runs these tasks on the same events as `quality`: a ready pull request,
later pushes while it has the `ci on every push` label, pushes to `main`, and runs by hand. It
writes the summary to the run page, uploads the HTML and XML reports as the `coverage-report`
artifact, and on a pull request keeps one comment up to date. A separate job with only
`pull-requests: write` posts that comment. Low coverage never fails the run; a failing test does,
and then there is no report.

To report on another open pull request without running `quality` again, run `coverage` by hand
and enter the pull request's number as `pr`. The run checks out that pull request's merge commit,
diffs it against its base, and posts or updates the comment on that pull request. Leave `pr`
empty to report on the branch the run starts from, without a comment.

`tools/coverage_report.py` writes the summary for every repository. It counts only handwritten
main Kotlin sources, so generated code such as SQLDelight queries and Compose resources is left
out.

- **Totals**: covered and total lines, branches, instructions, methods, and classes.
- **This pull request**: coverage of the lines the pull request adds or changes in main Kotlin
  sources, from `git diff -U0` against the merge base. Lines with no bytecode, such as comments,
  blank lines, and most declarations, do not count. A table lists each touched file with its
  uncovered changed lines, and a list names touched composables that no test renders.
- **UI**: how many public composables in the design system's `elements` and `components`, and in
  `presentation/screens` and `presentation/components`, a test renders. A composable counts as
  rendered when any code in its compiled function ran. Private helpers and previews do not count.
  Screens that no test renders are listed.
- **By module** and **By layer**: line coverage per module, for the design system's elements and
  components, and for `app`, `core`, and each module's or feature's `domain`, `data`, and
  `presentation` screens and components.
- **Least-covered files**: the 15 files with the most uncovered lines.

Limits:

- Only code that runs on the JVM counts, as above.
- Composables are matched to compiled methods by name, allowing for the suffixes Kotlin adds for
  value-class parameters and default arguments. Code inside composable and inline lambdas
  compiles to separate methods, so it counts toward lines but not toward whether a composable
  rendered. Inlined code counts where it is called.
- A composable whose name the scanner cannot find in the bytecode is reported as not found
  rather than as unrendered.

Measured in Meta on 2026-11-02 on `main` at `19f9973`:

| Module | Line | Branch |
| --- | --- | --- |
| `accounts` | 38.4% | 17.5% |
| `avatar` | 96.8% | 74.8% |
| `dateformat` | 89.9% | 95.5% |
| `designsystem` | 74.2% | 46.8% |
| `jmap` | 96.9% | 56.0% |
| `jmap-contacts` | 99.5% | 53.8% |
| `jmap-push` | 95.9% | 79.6% |
| `jscontact` | 97.8% | 79.1% |
| `settings` | 66.7% | 54.5% |
| All modules | 79.0% | 54.2% |

The `designsystem` module holds nearly three quarters of the measured lines, so it decides most
of the total. Screenshot tests raised it from 12.6% of lines; see
[screenshot tests](designsystem.md#screenshot-tests).

## Tool references

- [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-gradle)
- [detekt](https://detekt.dev/docs/1.23.8/introduction/configurations/)
- [Ruff](https://docs.astral.sh/ruff/)
- [SwiftFormat](https://github.com/nicklockwood/SwiftFormat)
- [SwiftLint](https://github.com/realm/SwiftLint)
