# Shared settings and accounts

Version 0.3.0 adds two Kotlin Multiplatform libraries used by Mail. Calendar
is unchanged. Both target Android and iOS; screens compose the common design system.

## Settings

`com.otus:settings` owns `SettingsScreen`, `AppearanceSettingsScreen`, `DateFormatSettingsState`,
date-format examples, and the `SettingsEntry` contract. The settings list receives ordered
entries with titles, summaries, icons, and callbacks. The app decides which pages exist and
where they lead.

The app supplies its own `DateFormatPreferenceStore`. Selection writes the store before
updating the displayed preference; an unsuccessful write does not report a saved change.
The appearance screen offers date formatting as a flat `SettingRow` with the pattern in a
`ValueButton`; tapping it opens a `ChoiceSheet`. Its trailing `content` slot places app-owned
sections below it, such as Mail's swipe actions. Dark mode remains the only theme.

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

HTTPS discovery, HTTP Basic authentication, explicit consent for a redirect to another host,
credential redaction, and app-private protected storage retain the existing Mail behavior.
The form holds its password in memory and clears it after a successful connection. Each
installed app has its own store; sharing the library does not share credentials.

## Boundaries and checks

Feature domain, data, and presentation remain separate. Screens may connect their state;
components take values and callbacks. Neither module knows Mail navigation or folders.
See [decision 0007](decisions/0007-shared-feature-modules.md).

Common tests cover address parsing, verification and storage failures, selected-service probes,
account state, and independent date-format preferences. Mail retains Compose integration tests
for settings navigation, persistence, account setup, and connection-to-inbox behavior.
