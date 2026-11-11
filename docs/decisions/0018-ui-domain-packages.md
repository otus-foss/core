# 0018: UI domain packages

## Status

Accepted by the user on 2026-10-27 for [Mail issue #174](https://github.com/otus-foss/otus-mail/issues/174).
Extended on 2026-10-28 to group theme code by responsibility.
The packages ship in 0.10.0.

## Decision

Keep one `com.otus:designsystem` module. Its production Kotlin packages use
`com.otus.designsystem.<shared|mail|calendar>.<group>.<elements|components>`. Create only groups
and layers that contain code. Calendar date pickers used by Mail belong in `shared/dates`; there
is no Calendar-app UI group yet.

Shared UI owns general controls and rendering reused by different features, including contacts,
people, date selection, list selection, folder visuals, sidebar, and filter controls. Mail groups
own message marks, editing, HTML reading, attachment display, and mail-folder compositions.
An existing name such as `SidebarInboxEntry` does not by itself make a value-driven renderer
Mail-specific. The dependencies and behavior determine its group.

Every element and component remains in Meta. Apps retain their screens, adapters, business
behavior, navigation, repositories, and installed-app data. The new folders do not introduce
business-domain layers inside the design system. The existing `accounts` and `settings` modules
keep their domain, data, and presentation boundaries.

The theme had 56 production files in one package. Group them under `theme/colors`, `motion`,
`brand`, `typography`, and `tokens/<family>`, while retaining `theme.OtusTheme` as the entry point.
Token families follow the rules they define, such as row layout or editing controls. They remain
independent of scoped UI even when only Mail currently uses them. Split mixed token files when
their declarations belong to different families. This changes packages, not token values.

## Dependency rules

- Elements cannot reference components in any group or design-system module.
- Shared UI cannot reference Mail or Calendar UI groups.
- Mail and Calendar UI groups may use shared UI but cannot reference each other.
- Theme code stays under the root `theme` subtree and cannot reference scoped UI. Shared renderers already use
  tokens with names such as `InboxTokens` and `ComposeTokens`; those names do not change ownership.
- Existing restrictions on app and feature references, networking, storage, services, platform
  APIs, package/path alignment, and literal colors still apply.

The architecture checker parses ownership, group, and UI layer instead of assuming the first
package segment is a layer. Regression tests cover the nested rules. It remains a source checker,
not a Kotlin dependency resolver; compilation and code review cover implicit and transitive uses.
Theme subpackages receive the same restrictions as the root, including dependency and platform
checks. Literal colors are permitted only inside the design-system theme subtree. A package
merely named `theme` elsewhere does not receive this permission.

## Migration

Move each file with its source-set counterparts and tests. Keep resource paths, the generated
`com.otus.designsystem.resources.Res` package, Android namespace, and screenshot identities.
Split mixed files only where declarations belong to different responsibilities. In particular,
shared person chips and row-selection values must remain available to accounts and contacts.

Public package moves require a breaking API changelog entry and the next minor version while
Meta is below 1.0. Update Meta's own consumers and the apps before adopting the new version.
The [migration guide](../ui-package-migration.md) records the moves and the cutover order.
