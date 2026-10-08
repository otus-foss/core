# 0007: Shared feature modules

Status: implemented for the requested Mail extraction on 2026-10-08. Release pending.

## Decision

The user explicitly requested extraction of existing shared settings and controls before
Calendar adoption. This allows these concrete modules to move ahead of the second consumer.

Share settings and account setup through the `settings` and `accounts` library modules in
otus-meta. These features have shared screens and state, rather than only reusable visual
primitives. Keeping their protocol, persistence, and screen behavior out of `designsystem`
preserves its contract of values and callbacks.

The earlier rule allowing Compose only in design-system modules is extended to the presentation
layers of these two named feature modules. Compose remains prohibited in protocol and service
modules. Elements and components remain free of repositories, ViewModels, network clients,
storage, and navigation controllers.

Shared features follow domain, data, and presentation boundaries. Domain has no UI or data
dependencies. Presentation depends on domain contracts; it does not use data implementations
or protocol clients. Composables live in components or screens, and screen filenames end in
`Screen.kt`. Shared features do not import one another. App wiring composes their contracts,
state, storage adapters, service requirements, and navigation callbacks.

The design system may use `dateformat` to display dates in the extracted date-range picker.
It cannot depend on either shared feature, protocol, or storage. Architecture regression tests
cover these rules; they do not replace compiler checks or semantic review.

## Ownership

Settings receives ordered `SettingsEntry` values. Mail supplies its Email accounts, Appearance,
Folders, and Rules entries. The library owns the list and appearance/date-format screen.
Account setup receives `AccountService`; Mail requires the mail capability and `Mailbox/get`.
Each app supplies its own login store and account-change callbacks. Library reuse does not
share credentials between installed apps.

The historical date-range picker and avatar-stack pill move to `designsystem`. Inbox filter
semantics and sender-to-avatar mapping stay in Mail. Calendar's checkout is unchanged.

## Release

The extraction is prepared as unreleased 0.3.0 and checked through the optional local composite
build. Publish the new libraries before merging Mail's version bump to main. Both repositories
remain independent and fresh clones consume released artifacts.
