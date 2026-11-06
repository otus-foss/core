# Shared settings and accounts

Version 0.3.0 adds two Kotlin Multiplatform libraries used by Mail. Calendar
is unchanged. Both target Android and iOS; screens compose the common design system.

## Settings

`com.otus:settings` owns `SettingsScreen`, `AppearanceSettingsScreen`, `DateFormatSettingsState`,
date-format examples, and the settings index. The app builds the index with `settingsIndex`:
top-level pages with summaries and icons, and inside each page its sections, settings, current
values, options, and subpages. The app decides which pages exist and where they lead. The home
lists the top-level pages. A search finds any page, setting, option, or keyword and groups the
results under their page. Keywords are other words people use for a setting, such as "Dark mode"
for Theme: `setting` and `toggle` take them, and a page names its own with `keywords(...)` in its
content. A query of several words also finds a setting that holds every word in some field, so
"swipe archive" finds the swipe whose option is Archive. Settings and pages that their page shows
disabled go inside `unavailable(reason) { ... }`, and their results show the reason in place of
their section. Opening a result calls the page's callback with the setting's title, and the app
provides it as `LocalFoundSetting` so the row flashes. `appearanceIndex` lists the Appearance
page's settings for the app's index.

`DependentSettings(enabled) { ... }` holds rows that only apply while another setting is set.
Unless enabled, each `SettingRow`, `SettingToggleRow`, and `SettingPageRow` inside stays where it
is, draws its title, icon, value, and switch dimmed, and ignores taps; give it a description that
says what it waits on. Prefer it to hiding the row, so the page keeps its shape and search still
finds the setting.

### Stored choices

The theme, app icon, animation speed, and date format are each a `StoredChoiceState<T>`:
`ThemeSettingsState`, `AppIconSettingsState`, `AnimationSettingsState`, and
`DateFormatSettingsState`. Each takes a `StoredChoice` from `com.otus:storage`, one text value
under a key in a `KeyValueStore`. The app chooses the file and key; `SharedPreferenceValues` keeps
them in Android shared preferences and `UserDefaultsValues` in iOS user defaults. Previews and
tests pass `inMemoryChoice()`.

Selection writes the choice before showing it, so a store that throws leaves the shown choice
unchanged. The platform stores save in the background, with `SharedPreferences.apply()` and
`NSUserDefaults`, so a failed save on disk is never reported, and the new choice may be lost after
a restart.

`com.otus:storage` also holds `StringMapStore`, a map read and replaced whole, with
`PreferenceMap` and `UserDefaultsMap`, and `storedName`, the SHA-256 name of a value for a file.
The appearance screen offers date formatting as a flat `SettingRow` with the pattern in a
`ValueButton`; tapping it opens a `ChoiceSheet`. Its trailing `content` slot places app-owned
sections below it, such as Mail's swipe actions. Its Look section starts with a Theme row that
shows the theme's name and accent and opens `ThemeSettingsScreen`, and an app adds its own rows
to the section through the `look` slot. `ThemeSettingsScreen` shows the app's preview, the four
themes as cards, the accent presets with a custom color sheet, and the three accent shades. A
choice applies at once and is written to the theme's `StoredChoice` first. Pass
`ThemeSettingsState.palette` to `OtusTheme`.

## Accounts

`com.otus:accounts` owns account models/contracts, JMAP server-address parsing,
`JmapAccountRepository`, `AccountsState`, `AccountSetupScreen`, and `ConnectedAccountRow`.
The app supplies its login store, transport, connection cache, coroutine scope, required
`AccountService`, and the `LoginLifecycle`s that follow each login. Screen titles are app
supplied.

`LoginLifecycle` (in `jmap`) has four steps, each a no-op unless overridden: `connected` after a
login is saved, `removing` while its credentials still exist, `removeFailed` when a removal stopped
and the login is still saved, and `removed` once it is gone. A `removing` that throws stops the
removal. Mail registers its cache, waiting changes, attachments, device data and push, and the
avatar service, so no wrapper repository has to undo a failed removal.

`AccountSetupScreen` takes the avatars it shows as an `AccountAvatars` and the avatar editor as a
slot, so `accounts` does not depend on `avatar`. Mail fills both from `avatar-ui`.

Mail defines its service with `JmapCapability.MAIL` and `Mailbox`. Setup checks the advertised
account capability and verifies
`Mailbox/get` before saving credentials, following
[RFC 8620](https://www.rfc-editor.org/rfc/rfc8620.html#section-2) and
[RFC 8621](https://www.rfc-editor.org/rfc/rfc8621.html#section-2).
No calendar or contact service is assumed. The generic contract lets a future consumer choose
its own capability and probe without introducing a provider-specific account flow.

HTTPS discovery, HTTP Basic authentication, explicit consent for a redirect, API endpoint, or
blob endpoint on another host, credential redaction, and app-private protected storage retain the existing Mail behavior.
The accounts page hides the form behind "Connect an account". The button opens the design system's
`SheetFrame`, with scrolling fields above the keyboard. A successful connection closes the sheet.
Done, the scrim, Back, and a downward swipe dismiss it and discard the draft. The form holds its
password in memory. Dismissal cancels pending connection work and drops credentials awaiting host
consent. Each
installed app has its own store; sharing the library does not share credentials.

Saved mail accounts discover sending addresses through `Identity/get`, following
[RFC 8621, section 6](https://www.rfc-editor.org/rfc/rfc8621.html#section-6). Discovery checks
submission support on both the session and the selected mail account. It never substitutes a
different submission account or requires submission to connect for mail. Rows show distinct
addresses below the account name, omit addresses already used as the name or sign-in, and retain
wildcard identities such as `*@example.org`. Receiving aliases without an identity are not exposed
by this method. Addresses refresh when accounts reload and are not persisted. A network, credential,
or identity error falls back to the saved sign-in and server. A service whose account cannot submit
mail finds no identities.
Each saved login's server is asked at the same time, so the list waits only for the slowest one and
keeps the saved order.

## Boundaries and checks

Feature domain, data, and presentation remain separate. Screens may connect their state;
components take values and callbacks. Neither module knows Mail navigation or folders.
See [decision 0007](decisions/0007-shared-feature-modules.md).

Common tests cover address parsing, verification and storage failures, selected-service probes,
account state, identity discovery and capability checks, and independent date-format preferences. Mail retains Compose integration tests
for settings navigation, persistence, account setup, and connection-to-inbox behavior.
