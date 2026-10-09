# Product goals

## Vision

Otus is a universal JMAP mobile app suite for mail and calendar, using JMAP Contacts across both apps. The goal is a consistent experience that connects to compatible providers through JMAP instead of being tied to one vendor's private API.

The suite contains two independently distributed apps:

- **Otus Mail**: the mail experience.
- **Otus Calendar**: the calendar experience.

“Universal client” is currently interpreted as provider independence and a coherent suite across these two apps, not a decision to merge them into one application. Both target Android and iOS using Kotlin Multiplatform and Compose Multiplatform.

## Platform priority

Android is the primary platform. Prioritize Android UX, device testing, performance, and release readiness. Preserve portable Kotlin boundaries and the iOS target; iOS remains a secondary platform.

## Protocol scope

JMAP is the intended protocol foundation for:

- Mail accounts, mailboxes, messages, and mail operations.
- Calendars, events, and calendar operations.
- Contacts used by the suite, including recipient and attendee selection.

Contacts are a shared suite capability; a third standalone Contacts app has not been requested. Exact contact editing, synchronization, and offline behavior are still to be designed.

Discover and respect the capabilities each account and server provides. Missing calendar or contacts capabilities must be handled explicitly rather than treated as universal server support. Select and document supported specification versions and interoperability targets before implementing each protocol area.

## Shared foundations

Aim to reuse account handling, JMAP transport, contact models, and appropriate UI components while preserving independent app repositories and releases. Mail-specific workflows belong in Mail; calendar-specific workflows belong in Calendar.

Shared libraries live in otus-meta and are published as versioned `com.otus:<module>` artifacts
to Forgejo Packages, with artifact migration still awaiting authenticated verification. Apps pin releases and may opt into a local composite build for development.
Shared runtime account data and credentials still require an explicit platform design;
library reuse alone does not provide them. See decision 0005.

## Current state

Both repositories contain Android/iOS scaffolding. Mail now starts with an empty Inbox screen, using the reusable context panel and local search/filter logic over supplied messages. A reusable sidebar navigates between Inbox and Settings. Settings has subpages for JMAP email accounts and appearance & behavior (date format). The sidebar, context panel, pills, and other reusable controls come from otus-meta's
design system. JMAP transport/login storage, dates, and avatars also come from shared libraries.
Version 0.3.0 adds shared settings/account setup and date-range selection,
used by Mail's consuming PR. Mail still owns its destinations, filter semantics, and inbox/folder
flows; Calendar's adoption is deferred. Meta also owns all Mail visual tokens and symbols,
plus value-driven message/detail rows and empty-state rendering. New branded elements and
reusable visual compositions are developed in Meta. Mail connects JMAP accounts over HTTP Basic auth, stores credentials in platform-protected storage, and loads each account's inbox. Folders (JMAP mailboxes) appear in the sidebar, open as pages, and can
be created, renamed, nested, and deleted; messages can be moved between them. Rules that file
arriving mail into folders run on the server as one Sieve script (RFC 9661) where the server
supports it; see the [Mail folders guide](https://git.hilfikers.com/florian.ruby/otus-mail/src/branch/main/docs/folders.md). Sync and sending are not
implemented yet. No sample mail is shipped; populated-list validation uses test-only fixtures. Calendar retains its welcome screen. Full synchronization, calendar workflows, and contact management remain goals. Mail can now look up
JMAP contact photos for inbox senders; this is a limited read-only lookup, not contact synchronization.
Both apps expose the same portable avatar service for senders and future attendees. Domain icons
and external contact URLs are configurable service options, enabled for Mail inbox senders, with
generated initials always available.

Mail colors are supplied in [mail design foundations](mail-design.md). Both apps use that palette in dark mode only, with text-1 as the default text color. Detailed interaction designs remain open.

Both apps include reusable date-format choices, locale-aware formatting, and app-private preference
adapters. Mail uses them for displayed inbox dates and date-filter summaries. Its Settings and
Appearance & behavior pages let users choose the saved date format. Calendar settings remain
unimplemented. See the repository-local [Mail guide](https://git.hilfikers.com/florian.ruby/otus-mail/src/branch/main/docs/date-formatting.md) and
[Calendar guide](https://git.hilfikers.com/florian.ruby/otus-calendar/src/branch/main/docs/date-formatting.md).

## Decisions to resolve as implementation starts

- Initial provider/server compatibility targets and supported JMAP capabilities.
- Authentication and account experience across the two apps.
- Local persistence, offline behavior, and synchronization strategy.
- Contact management scope and calendar-specific design tokens.
