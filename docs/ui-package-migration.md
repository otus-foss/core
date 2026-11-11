# UI package migration for 0.10.0

Version 0.10.0 moves the design system's flat `elements` and
`components` packages beneath an owner and a UI group. It also groups theme declarations by
responsibility. The artifact remains `com.otusfoss.core:designsystem`. `theme.OtusTheme` and generated
resource identifiers retain their packages.

## Updating imports

Use the [declaration mapping](ui-package-moves.csv) to replace each old import with its new
qualified name. A package-wide text replacement is insufficient because declarations from one
old package now belong to several groups. The mapping includes moved public and internal
declarations, including extension functions and values.

```kotlin
import com.otusfoss.core.designsystem.shared.contacts.components.ContactRow
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import com.otusfoss.core.designsystem.shared.rows.components.RowSelection
import com.otusfoss.core.designsystem.mail.read.elements.HtmlView
import com.otusfoss.core.designsystem.theme.OtusTheme
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.Motion
import com.otusfoss.core.designsystem.theme.tokens.rows.InboxTokens
```

Keep existing public declaration names. The migration changes source organization, not visual
behavior or business policy. It retains screenshot baselines and the Android namespace.

## Placing files

The package mirrors `designsystem/src/<sourceSet>/kotlin/com/otusfoss/core/designsystem/`:

```text
theme/OtusTheme.kt
theme/colors/
theme/motion/
theme/brand/
theme/typography/
theme/tokens/<family>/
shared/<group>/elements/
shared/<group>/components/
mail/<feature>/elements/
mail/<feature>/components/
```

Shared groups own controls, inputs, people, contacts, dates, rows, folder visuals, selection,
overlays, filters, action bars, panels, sidebar, page layout, settings, feedback, reordering,
and swipes. Mail groups own inbox rendering, composition, reading, folder compositions,
attachments, and launch animation. Create only directories that contain code.

Theme families are `layout`, `overlays`, `rows`, `selection`, `people`, `editing`, `content`,
`feedback`, and `settings`. For example, `InboxTokens` moves to `theme.tokens.rows`,
`ComposeTokens` to `theme.tokens.editing`, and `ReadTokens` to `theme.tokens.content`.
`MoveTokens` and `FolderRowTokens` now have their own files in `selection` and `rows`.
All remain theme code that shared UI can use. Palette and preference types, including
`OtusPalette` and `ThemeChoice`, move to `theme.colors`; their values and stored formats stay the same.

Keep a renderer's display values, visual state, and layout calculations beside it. Split a file
when its declarations have different owners. The shared `Recipient` display value and person
chips remain usable by accounts and contacts; the editable recipient line belongs to Mail.
Shared `RowSelection` stays usable by contact and message lists.

The existing calendar-named files implement date selection, so they belong in `shared/dates`.
A future Calendar-app renderer can use `calendar/<feature>/<layer>`. Do not create empty groups.
See [decision 0018](decisions/0018-ui-domain-packages.md) for the enforced dependency rules.

## Platform code, tests, and assets

Platform implementations keep the same relative packages as their common declarations. Move
the avatar decoding, HTML view, keyboard visibility, and sheet dialog-property families together.
Common tests and Android host tests mirror the code they cover. Test fixtures keep responsibility
names and can support several groups.

Leave Compose resources, Android resources, fonts, `designsystem/brand`, and generated
`com.otusfoss.core.designsystem.resources.Res` where they are. Screenshot files keep their explicit
names under `designsystem/src/androidHostTest/screenshots`; verify them without recording.

## Coordinating the repositories

Core updates `accounts` and `settings` in the same change as the design system. Mail adopts the
new imports on a branch with the same name, which its quality workflow builds against the
matching Core branch.

Mail's nightly builds Mail main against Core main. Merge the Core change and Mail's import
update one after the other, away from the nightly's 00:00 UTC run, so no nightly pairs migrated
Core with old imports.

## Validation

Run the architecture regression tests, formatting and static checks, Android host tests with
existing screenshots, and `checkAll`. Compile both iOS library targets. The architecture checker
rejects old production UI packages and enforces the new nested boundaries.

Validate Mail and Calendar against the candidate through source substitution and through locally
published artifacts. Use `otus.core.repository` for the artifact check so a machine-wide
`otus.core.path` cannot silently substitute source. The apps' candidate version pins must match
the candidate repository. Run Gradle sequentially through the shared-machine build queue.

Linux compilation does not verify Xcode linking or iOS resource loading. Run those checks on
macOS before release. Record any unavailable platform checks in the PR; do not report them as
passing.

### Candidate validation on 2026-10-27

Core, Mail, and Calendar passed `checkAll` with the migrated Core source. Both apps and Core's
design system compiled their `iosArm64` and `iosSimulatorArm64` Kotlin libraries. Existing
screenshot baselines remain unchanged.

Local publication failed at `:settings:compileIosMainKotlinMetadata` because Compose dependencies
produce duplicate Kotlin library names. The same failure occurs on unchanged Core `b1a860f`,
before the package migration. The candidate repository is incomplete, so packaged-artifact
consumer checks remain blocked. Resolve that publication failure and rerun the artifact checks
before releasing 0.10.0. Xcode linking, simulator tests, and iOS resource loading remain unrun.

### Theme and JSContact follow-up on 2026-10-28

Core passed `checkAll`, including 424 design-system tests and 165 JSContact tests. Both libraries
compiled their `iosArm64` and `iosSimulatorArm64` targets. After the final checker correction,
`qualityCheck` passed all 94 tooling tests, including 61 architecture tests.

Mail at `c52820b3`, with the theme import changes, passed `checkAll` and both iOS compilations
against the candidate source. Its 888 shared tests and 335 Android tests passed. Unchanged
Calendar at `ac24487` also passed `checkAll` and both iOS compilations. Screenshot baselines and
visual assets remain unchanged.

The design system, date formatting, and JSContact published into an isolated local Maven
repository. All 12 root/platform publications passed metadata and checksum verification. This
does not resolve the full-suite settings publication blocker described above or validate an
app against a complete packaged release.
