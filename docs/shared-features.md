# Shared settings and accounts

Version 0.3.0 adds two Kotlin Multiplatform libraries used by Mail. Calendar
is unchanged. Both target Android and iOS; screens compose the common design system.

## Settings

`com.otus:settings` owns `SettingsScreen`, `AppearanceSettingsScreen`, `DateFormatSettingsState`,
date-format examples, and the `SettingsEntry` contract. The settings list receives ordered
entries with titles, summaries, icons, and callbacks. The app decides which pages exist and
where they lead.

The app supplies its own `DateFormatPreferenceStore`. Selection calls the store's `write` before
updating the displayed preference, so a `write` that throws leaves the shown choice unchanged.
`write` returns nothing, though, and the provided stores save in the background:
`AndroidDateFormatPreferenceStore` with `SharedPreferences.apply()` and
`IosDateFormatPreferenceStore` with `NSUserDefaults`. A failed save on disk is never reported, so
the new choice shows as saved and may be lost after a restart. `AnimationSettingsState` and the
animation stores work the same way.
The appearance screen offers date formatting as a flat `SettingRow` with the pattern in a
`ValueButton`; tapping it opens a `ChoiceSheet`. Its trailing `content` slot places app-owned
sections below it, such as Mail's swipe actions. Its Look section starts with a Theme row that
shows the theme's name and accent and opens `ThemeSettingsScreen`, and an app adds its own rows
to the section through the `look` slot. `ThemeSettingsScreen` shows the app's preview, the four
themes as cards, the accent presets with a custom color sheet, and the three accent shades. A
choice applies at once and is written to the app's `ThemePreferenceStore` first. Pass
`ThemeSettingsState.palette` to `OtusTheme`.

## Accounts

`com.otus:accounts` owns account models/contracts, JMAP server-address parsing,
`JmapAccountRepository`, `AccountsState`, `AccountSetupScreen`, and `ConnectedAccountRow`.
The app supplies its login store, transport, connection cache, coroutine scope, account-change
callbacks, and required `AccountService`. Screen titles are app supplied.

Mail uses `AccountService.Mail`. Setup checks the advertised account capability and verifies
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
or identity error falls back to the saved sign-in and server. Other services skip identity discovery.
Each saved login's server is asked at the same time, so the list waits only for the slowest one and
keeps the saved order.

## Boundaries and checks

Feature domain, data, and presentation remain separate. Screens may connect their state;
components take values and callbacks. Neither module knows Mail navigation or folders.
See [decision 0007](decisions/0007-shared-feature-modules.md).

Common tests cover address parsing, verification and storage failures, selected-service probes,
account state, identity discovery and capability checks, and independent date-format preferences. Mail retains Compose integration tests
for settings navigation, persistence, account setup, and connection-to-inbox behavior.
