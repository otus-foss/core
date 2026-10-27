# UI package migration for 0.10.0

Version 0.10.0 moves the design system's flat `elements` and
`components` packages beneath an owner and a UI group. The artifact remains
`com.otus:designsystem`. Theme tokens and generated resource identifiers retain their packages.

## Updating imports

Use the [declaration mapping](ui-package-moves.csv) to replace each old import with its new
qualified name. A package-wide text replacement is insufficient because declarations from one
old package now belong to several groups. The mapping includes moved public and internal
declarations, including extension functions and values.

```kotlin
import com.otus.designsystem.shared.contacts.components.ContactRow
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.rows.components.RowSelection
import com.otus.designsystem.mail.read.elements.HtmlView
```

Keep existing public declaration names. The migration changes source organization, not visual
behavior or business policy. It retains screenshot baselines and the Android namespace.

## Placing files

The package mirrors `designsystem/src/<sourceSet>/kotlin/com/otus/designsystem/`:

```text
theme/
shared/<group>/elements/
shared/<group>/components/
mail/<feature>/elements/
mail/<feature>/components/
```

Shared groups own controls, inputs, people, contacts, dates, rows, folder visuals, selection,
overlays, filters, action bars, panels, sidebar, page layout, settings, feedback, reordering,
and swipes. Mail groups own inbox rendering, composition, reading, folder compositions,
attachments, and launch animation. Create only directories that contain code.

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
`com.otus.designsystem.resources.Res` where they are. Screenshot files keep their explicit
names under `designsystem/src/androidHostTest/screenshots`; verify them without recording.

## Coordinating the repositories

Meta updates `accounts` and `settings` in the same change as the design system. Mail adopts the
new imports on a branch with the same name, which its quality workflow builds against the
matching Meta branch.

Mail's nightly builds Mail main against Meta main. Merge the Meta change and Mail's import
update one after the other, away from the nightly's 00:00 UTC run, so no nightly pairs migrated
Meta with old imports.

## Validation

Run the architecture regression tests, formatting and static checks, Android host tests with
existing screenshots, and `checkAll`. Compile both iOS library targets. The architecture checker
rejects old production UI packages and enforces the new nested boundaries.

Validate Mail and Calendar against the candidate through source substitution and through locally
published artifacts. Use `otus.meta.repository` for the artifact check so a machine-wide
`otus.meta.path` cannot silently substitute source. The apps' candidate version pins must match
the candidate repository. Run Gradle sequentially through the shared-machine build queue.

Linux compilation does not verify Xcode linking or iOS resource loading. Run those checks on
macOS before release. Record any unavailable platform checks in the PR; do not report them as
passing.

### Candidate validation on 2026-10-27

Meta, Mail, and Calendar passed `checkAll` with the migrated Meta source. Both apps and Meta's
design system compiled their `iosArm64` and `iosSimulatorArm64` Kotlin libraries. Existing
screenshot baselines remain unchanged.

Local publication failed at `:settings:compileIosMainKotlinMetadata` because Compose dependencies
produce duplicate Kotlin library names. The same failure occurs on unchanged Meta `b1a860f`,
before the package migration. The candidate repository is incomplete, so packaged-artifact
consumer checks remain blocked. Resolve that publication failure and rerun the artifact checks
before releasing 0.10.0. Xcode linking, simulator tests, and iOS resource loading remain unrun.
