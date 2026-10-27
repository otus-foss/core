# UI package migration for 0.10.0

Version 0.10.0 is a release candidate. It moves the design system's flat `elements` and
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
new imports on its matching `refactor/ui-domains` branch. Its quality workflow can select the
Meta branch with the same name while the candidate is under review.

Mail's nightly normally combines Mail main and Meta main. Before landing incompatible package
moves, merge the separate preparation that pins Meta to the compatible pre-migration revision.
Keep the nightly's resolved SHA in its provenance. The Mail consumer PR must restore main
tracking in the same merge that adopts the migrated imports. Base it on the nightly-pin
preparation so the removal remains an explicit change after that preparation merges.

Publishing remains paused. Under the normal release policy, publish 0.10.0 before merging a
consumer version pin that requires its packages. A local candidate repository proves artifact
compatibility but does not make the version available on GitHub Packages. Keep the consumer PR
in draft until the release dependency is satisfied. See [releasing](releasing.md).

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
