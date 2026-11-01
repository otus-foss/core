# Otus Meta engineering rules

## Hosting

GitHub hosts development, pull requests, issues, CI, and released packages. Use `origin` for
GitHub. See [hosting and CI](docs/hosting.md).

## Required reading and commands

Read `docs/architecture.md`, `docs/quality.md`, and `docs/releasing.md` before changing code. Install
the Python tools as described in the quality guide, then run `./gradlew checkAll` before handing
work back. Use `./gradlew formatAll` for formatting. Never report an unrun check as passing.

## Purpose

This repository holds the Kotlin Multiplatform libraries shared by Otus Mail and Otus Calendar,
plus suite-wide documentation and decisions. Android is the primary platform; keep every module
compiling for iOS. Read [the suite product goals](docs/suite/product-goals.md) and
[mail design foundations](docs/suite/mail-design.md) for product direction and the palette.

## Library rules

- One module per top-level directory. Its package is `com.otus.<module>`, with `-` in the
  directory name becoming `.` in the package. Its artifact is `com.otus:<module>`.
- No module references Mail or Calendar code. App-specific screens, feature wiring, navigation, database
  instances, notifications, and credentials stay in the apps.
- All branded visual elements, reusable components, tokens, and symbols belong here, including
  those currently used only by Mail. Feature adapters and app-specific screens stay in the apps.
  See decision 0008.
- Move existing code here when both apps need it or the user explicitly requests a suite
  extraction. Do not add speculative modules or placeholder files.
- Enable explicit API mode and warnings as errors in every module. Public declarations are a
  contract with both apps; change them only with a changelog entry and the right version bump.
- `designsystem` modules follow **elements → components**, with tokens in `theme` and values
  and callbacks at their public boundary. Put UI in `shared/<group>/elements` or `components`,
  or in `mail/<feature>/elements` or `components` for mail-specific rendering. Add a `calendar`
  group only when it contains Calendar-specific UI. Shared UI cannot depend on Mail or Calendar
  UI groups; Mail and Calendar UI groups cannot depend on each other. Elements cannot depend on
  components in any group. Keep theme code under the root `theme` subtree, grouped by
  responsibility and independent of scoped UI. Keep `OtusTheme` at that subtree's root. See
  [decision 0018](docs/decisions/0018-ui-domain-packages.md).
  The shared `accounts` and `settings` feature modules
  may use Compose only in `presentation`, with composables in `components` or `screens`. Keep
  their domain, data, and presentation layers separate; design-system code cannot depend on
  features. See decision 0007. Use named palette tokens; they follow the theme the user picks, so
  read them when drawing and never copy them into stored values. See decision 0014.
- Animate only through `Motion`. Use `motionTween(speed, curve)` in composables,
  `Motion.tween(speed, curve, scale)` outside composition, or `Motion.millis(speed, scale)` for a
  spec that is not a tween, and respect `LocalMotionScale` everywhere. Never add a raw duration
  constant, a `tween` with a literal, a new `CubicBezierEasing`, or an animation with a Compose
  default spec. Pick the speed by what moves: `Quick` for touch feedback, `Fast` for small
  in-place changes, `Normal` for panels, sheets, and text, `Relaxed` for large surfaces such as
  the sidebar or a settling row. Pick the curve by direction: `Standard` by default, `Enter` for
  things arriving, `Exit` for things leaving. Change `LocalMotionScale` to speed everything up or
  down; do not edit the speeds. See [the design system guide](docs/designsystem.md#motion).
- `commonMain` uses no platform APIs. Platform code goes in matching platform source sets.
  Domain code uses no UI, platform, networking, or database APIs.
- Keep JMAP wire types separate from portable domain models. Check the JMAP specifications and
  advertised server capabilities before implementing protocol behavior.
- Keep `gradle/libs.versions.toml` aligned with the apps. Do not publish with a Kotlin version
  newer than the one the apps use.
- Write fluent Kotlin when it reads better than the alternative. Express transformations as
  collection or sequence chains, such as `filter`, `map`, `associateBy`, and `firstNotNullOfOrNull`,
  or as builder lambdas such as `buildList`, `buildMap`, and `buildJsonObject`, instead of mutable
  accumulators and index loops. Make composable configuration a chainable extension that returns its
  receiver, like `fun Modifier.monthBackgrounds(...): Modifier`, so call sites read as one `Modifier`
  chain. Change immutable values with `copy`. Keep a loop or a local `var` when early exits, retries,
  or cycle tracking would make a chain harder to follow. Do not add Java-style setters that return
  `this`, chain side effects through `let` or `also`, or change a public API only for style.
  Public APIs follow the same idea: callers should configure a component through
  `Modifier` chains, named arguments, and value types rather than mutable setup objects.
- Keep a call chain as one expression, with one call per line, instead of splitting it into
  temporary `val`s that are each read once. Use a builder or chainable API wherever the library
  offers one and it fits, such as `SharedPreferences.Editor`, Ktor request builders, and
  `buildJsonObject`. Name an intermediate value only when the code reads it more than once or the
  name says something the chain cannot.

Do not add blanket suppressions, formatter-off markers, lint baselines, skipped checks, or
failure-tolerant CI to make a change pass. A rule change needs an explicit rationale in
`docs/decisions/` and a corresponding check/test update. Tool versions must be pinned. Keep
handwritten Kotlin files at most 300 lines; see `docs/quality.md` for the exception directive.

## Readable code

Write for the person who reads the code next. A file passing ktlint does not make it readable:
ktlint adds braces and line breaks, but it never takes them out again once the code gets shorter.
After formatting, read the diff and fix shapes like these.

- Give every `when` entry one line. ktlint_official braces every entry once one entry needs
  several lines, and it leaves the braces after that entry shrinks. Remove leftover braces, or move
  a long entry's body into a named function so all entries fit on one line.

  ```kotlin
  // Avoid
  when {
      created < 0 -> {
          null
      }

      kind == MISS_RECORD && bytes.isEmpty() -> {
          StoredAvatar(null, created)
      }
  }

  // Prefer
  when {
      created < 0 -> null
      kind == MISS_RECORD && bytes.isEmpty() -> StoredAvatar(null, created)
  }
  ```

- Keep nesting to two or three levels. Return early with `?: return` instead of wrapping a body in
  `if (x != null) { ... }` or `?.let { ... }`, and give a lambda nested several levels deep its
  own named function or composable, such as `decodedRecord(...)` or `StarButton(star)`.
- Join a call that fits in 120 columns back onto one line. When it does not fit, name a part of it
  first, such as `val fitted = ...` or `val header = ...`, rather than splitting
  `foo(\n    x,\n)?.let { ... }` over several lines.
- Write `Modifier.then(if (x) Modifier else Modifier.a().b())` as a named private extension, such
  as `Modifier.updateButton(...)` or `Modifier.progressRing(...)`.
- Name arguments that are bare literals: `ChipFrame(selected = null, enabled = true)`, not
  `ChipFrame(null, true)`.
- Never hide a side effect in `(...).also { if (it) save() }` or `x?.let { destroy(it) }`. Write a
  plain `if`.
- Rewrap KDoc so each paragraph reads as prose. Do not leave a sentence broken after a long link.
- Name values by what they hold. Do not shadow a parameter or a standard function such as `run`.

Do not churn code to meet these rules. Fix the shapes in code you touch.

## Commit messages

Use this format for authored commits and squash commit messages:

```text
type(scope): short action

- Explain a concrete change.
- Explain another change or why it was needed.
```

- Always include a type and scope. Use lowercase for both; use kebab-case for multiword scopes.
- Choose the type by the purpose of the change: `feat` for new behavior, `fix` for a bug,
  `refactor` for restructuring without behavior changes, `perf` for performance work,
  `docs` for documentation, `test` for tests, `build` for dependencies or build tooling,
  `ci` for automation, `chore` for other maintenance, and `revert` for undoing a commit.
- Name the affected area in the scope, such as `inbox`, `accounts`, `avatars`, `design-system`,
  `jmap`, `designsystem`, `docs`, `release`, or `build`. Scopes are open-ended. Choose the most
  specific area that describes the whole commit; split unrelated changes into separate commits.
- Start the subject with a lowercase imperative verb, such as `add`, `fix`, `remove`, or
  `simplify`. Preserve the spelling of code identifiers. Aim for at most 72 characters in the
  entire title, and omit a final period. Avoid vague subjects such as `update stuff`.
- Leave one blank line after the title. Require 1–8 body bullets, each starting with `- `.
  Explain what changed; include the reason when it helps. Use only as many bullets as needed.
  Do not pad the body, repeat the title, or turn it into a file inventory.
- Describe only changes in the commit. Mention checks only if they actually ran, and report
  their result accurately. Do not claim planned behavior or unverified performance gains.
- Apply the `unslop` skill to the entire message when available. Always use plain, concrete
  wording without filler, decorative emojis, or AI stock phrases. Keep the required syntax.
- For a breaking change, put `!` after the scope and explain the impact and required migration
  in the bullets. Optional issue references or `BREAKING CHANGE:` footers follow the bullets,
  separated by a blank line; they do not count toward the bullet limit.

Example:

```text
feat(inbox): add avatars

- Show contact photos beside inbox senders.
- Fall back to initials when a photo is unavailable.
```

Enable the versioned Git hooks once per checkout, including new clones. `pre-commit` checks
formatting, and `commit-msg` checks the message and removes AI attribution lines. No hook runs
`checkAll`, and CI checks only pull requests, so run `./gradlew checkAll` yourself before a push
that needs it, such as a direct push to `main` or a release tag.

```sh
git config --local core.hooksPath .githooks
```

Run the command from this repository's root. The `commit-msg` hook needs Python 3.11+ and rejects invalid
title syntax, missing separators, and bodies outside the 1–8 bullet limit. Review wording,
imperative verbs, breaking-change explanations, and the
72-character target manually. Git-generated merge messages and temporary autosquash messages are
exempt; the final squash message must follow the rules. CI tests the checker but does not inspect
commit history. See [the decision](docs/decisions/0004-commit-messages.md).

## Pull requests

Write every pull request from `.github/pull_request_template.md` and keep its headings in the same
order. GitHub fills it in on the website, but `gh pr create --body` skips it, so copy the headings
when you write the body yourself. The title follows the commit title rules above. Leave out
"Generated with" footers and session links. `gh` cannot upload images, so link recorded
screenshots at the head commit, and send phone captures to the user to drag in.

## Agent workflow

During development, prefer `./gradlew spotlessCheck detekt ruffCheck ruffFormatCheck` and run
only tests relevant to the change, for example
`./gradlew :dateformat:testAndroidHostTest --tests 'com.otus.dateformat.DateFormatterTest'`.
Use `qualityCheck` when all quality checks are needed. Batch edits and run `checkAll` once at the
end, rather than after each edit. Run it again only if later changes or failures require it.

Inspect existing patterns first. Make focused changes, run the relevant checks, and report any
environment blockers precisely. Add behavior tests for new logic and regression tests for fixes.
Update `docs/architecture.md` when modules or boundaries change.

Mail, Calendar, and this repository are separate Git repositories. Never assume a sibling checkout
in CI. When a change here needs app changes, release it first, then update each app in its own
repository. Do not publish a release, push tags, or change package access without a concrete task.
