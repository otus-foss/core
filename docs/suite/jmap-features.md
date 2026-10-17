# JMAP features in Otus

This guide is for agents and people who design new screens, settings, or actions. It lists each
JMAP capability, data type, and method, and says whether Otus uses it today, has it in progress,
plans it, or has no plan for it. Read it before you add a control that reads or changes server
data.

The library API is in [the JMAP library guide](../jmap.md). Mail's folder, rule, and Read later
behavior is in [Mail's folders guide](https://github.com/reazndev/otus-mail/blob/main/docs/folders.md).
The v1 plan is the [suite roadmap](https://github.com/reazndev/otus-mail/issues/9).

Last checked against the code on 2026-10-12: Mail and Meta `main`, Calendar `main`, and the
uncommitted `feat/action-bar` Mail worktree.

## Status labels

| Label | Meaning |
| --- | --- |
| Used | On `main` in Mail or Meta, with tests. |
| In progress | Written on a feature branch, not merged. Do not design against it as if it shipped. |
| Planned | An open roadmap issue covers it. The design is not settled. |
| No plan | Nothing uses it and no issue asks for it. Propose it in an issue before you design for it. |

## Rules for designing against JMAP

1. **Check the capability before you show a control.** A server that offers mail may not offer
   Sieve, submission, contacts, or calendars. Hide the control, or show why it is unavailable,
   when the session or the account lacks the capability. Rules, for example, need
   `urn:ietf:params:jmap:sieve` plus the `fileinto` and `mailboxid` Sieve extensions.
2. **Resolve the account per capability.** One login can have a different JMAP account for mail,
   contacts, and calendars. Use `JmapSession.accountFor(capability)`. Never reuse the mail account
   id for contacts.
3. **Follow `myRights` and the advertised limits.** Disable or hide actions the server will refuse:
   `mayRename`, `mayDelete`, `mayCreateChild`, `mayAddItems`, `mayRemoveItems`,
   `mayCreateTopLevelMailbox`, `maxMailboxDepth`, `maxSizeMailboxName`, `maxMailboxesPerEmail`,
   `maxObjectsInSet`, and `maxSizeUpload`. A missing right reads as allowed, and the server still
   decides.
4. **Find system folders by role, never by name.** Servers name them differently. Stalwart calls
   them "Sent Items", "Junk Mail", and "Deleted Items". The app names `inbox`, `drafts`, `sent`,
   `archive`, `junk` (shown as Spam), and `trash` itself.
5. **Keep every action inside one account.** A message, folder, or rule belongs to one login. A
   unified view can list several accounts, but a move or a rule cannot cross them.
6. **Know what is local.** Search, filters, pills, swipe choices, and display settings run on the
   device over loaded messages. They do not change the server. Folders, keywords, and rules do,
   and other mail apps see the change.
7. **Do not assume Stalwart.** Otus is developed against Stalwart, but common flows must not depend
   on its quirks. Record a server quirk in the feature's guide, as the folders guide does for the
   `vacation` script.

## Capabilities

| Capability | Spec | Status | What uses it |
| --- | --- | --- | --- |
| `urn:ietf:params:jmap:core` | RFC 8620 | Used | Session discovery, every request, blob upload and download. |
| `urn:ietf:params:jmap:mail` | RFC 8621 | Used | Account setup check, inbox, folders, message actions. |
| `urn:ietf:params:jmap:submission` | RFC 8621 | Used | Sending addresses on the accounts page and in compose. Sending is in progress on Mail `feat/send`. |
| `urn:ietf:params:jmap:sieve` | RFC 9661 | Used | Rules in Mail. |
| `urn:ietf:params:jmap:contacts` | RFC 9610 | Used, read only | Contact photos for inbox senders. Contact management is planned. |
| `urn:ietf:params:jmap:calendars` | IETF draft | Planned | Calendar app. The constant exists in `JmapCapability`; no code calls it. |
| `urn:ietf:params:jmap:vacationresponse` | RFC 8621 | No plan | Rules only detect Stalwart's `vacation` Sieve script. |
| `urn:ietf:params:jmap:quota` | RFC 9425 | No plan | |
| `urn:ietf:params:jmap:mdn` | RFC 9007 | No plan | |
| `urn:ietf:params:jmap:websocket` | RFC 8887 | No plan | Push uses Web Push and EventSource. See [push](#push-and-sync). |
| `urn:ietf:params:jmap:webpush-vapid` | RFC 9749 | Planned | Lets Google's FCM accept pushes without a gateway. The library reads the key. |
| `urn:ietf:params:jmap:principals` and sharing | RFC 9670 | Planned | Shared calendars ([Calendar #9](https://github.com/reazndev/otus-calendar/issues/9)). |
| Tasks | IETF draft | No plan | The roadmap defers a task manager. `TaskList/get` appears only in library tests. |

## Session and core

| Feature | Status | Notes |
| --- | --- | --- |
| `/.well-known/jmap` discovery, redirects | Used | https only. A redirect to another host asks the user first. |
| HTTP Basic auth | Used | The only sign-in method. |
| OAuth | Planned | [Mail #13](https://github.com/reazndev/otus-mail/issues/13). Do not design OAuth screens until it is decided. |
| Result references in one request | Used | `Email/query` then `Email/get`; `ContactCard/query` then `ContactCard/get`. |
| `ifInState` on `/set` | Used | Saving rules. A `stateMismatch` makes the app reload and ask the user to save again. |
| Blob upload | Used | Sieve scripts. Attachments are planned. |
| Blob download | Used | Contact photos. Attachments are planned. |
| `/changes` | Planned | [Mail #15](https://github.com/reazndev/otus-mail/issues/15). Today every load refetches. |
| `/queryChanges` | Planned | Mail #15, where the server supports it. |
| `/copy` | No plan | No cross-account copy or move. |
| `Core/echo` | Library tests only | |
| `maxCallsInRequest` and other request limits | Planned | The library does not enforce them. Mail #12 plans to respect them. |

## Mail

### Mailbox (folders)

| Method or field | Status | Notes |
| --- | --- | --- |
| `Mailbox/get` | Used | Properties: `id`, `name`, `parentId`, `role`, `sortOrder`, `totalEmails`, `unreadEmails`, `myRights`. |
| `Mailbox/set` create, rename, move, delete | Used | Settings › Folders. Only folders without a role can be renamed, moved, or deleted. |
| `isSubscribed` | Used on create only | New folders are created subscribed so IMAP clients list them. Otus never reads it and has no subscribe toggle. |
| `onDestroyRemoveEmails` | Used, always `false` | A folder with mail or subfolders cannot be deleted. The user empties it first. |
| `Mailbox/query` | In progress | `feat/action-bar` only. |
| `Mailbox/changes` | Planned | Mail #15. |
| Shared mailboxes (`isSubscribed`, `shareWith`) | No plan | |

Folder features built on these: the sidebar tree with unread counts, folder pages, the optional
Read later folder (a normal folder without a role, tracked by id), and Combined folders (one
sidebar entry per role across accounts). These are app features, not JMAP features. Another mail
app sees Read later as an ordinary folder.

### Email (messages)

| Method or field | Status | Notes |
| --- | --- | --- |
| `Email/query` | Used | Filter `inMailbox` only, sorted by `receivedAt` descending, newest 50 per mailbox. No server text search. |
| `Email/get` | Used | Properties: `id`, `mailboxIds`, `keywords`, `from`, `subject`, `receivedAt`, `preview`, `hasAttachment`, `attachments`, `textBody`, `bodyValues`. Text bodies up to 20,000 bytes per part. |
| `Email/set` keyword patch | Used | Read and unread (`$seen`), flagged (`$flagged`). |
| `Email/set` `mailboxIds` patch | Used | Move, Archive, Trash, Read later, rule backfill. Batched within `maxObjectsInSet`. |
| `Email/set` create and destroy drafts | In progress | Compose saves to the `drafts` role folder on `feat/action-bar`. |
| HTML bodies (`htmlBody`) | Partial | Mail renders an open message's HTML body in `HtmlView` with scripts off and remote images hidden until the user shows them. Inline `cid:` images are not loaded yet. See [Mail #25](https://github.com/reazndev/otus-mail/issues/25). |
| Headers beyond From and Subject (`to`, `cc`, `messageId`, `references`) | In progress | Compose reads them for replies on `feat/action-bar`. |
| Attachment download | Planned | Mail #25. Rows show attachment names only. |
| Server-side search (`text`, `from`, `subject` filters) | Planned | [Mail #23](https://github.com/reazndev/otus-mail/issues/23) plans local and server evaluation. Today search filters loaded messages on the device. |
| `$answered`, `$forwarded`, `$draft` keywords | In progress | Drafts carry `$draft`. On Mail `feat/send`, sending clears it and sets `$answered` or `$forwarded` on the original. |
| `$junk` and `$notjunk` keywords | No plan | A Junk action is planned in Mail #25. It may move to the `junk` role folder, set keywords, or both. Decide in that issue. |
| Custom keywords as labels | No plan | Labels in rows come from folders, not keywords. |
| `Email/import`, `Email/parse`, `Email/copy` | No plan | |
| `Email/changes`, `Email/queryChanges` | Planned | Mail #15. |

### Thread and SearchSnippet

| Type | Status | Notes |
| --- | --- | --- |
| `Thread/get`, `threadId` | Partial | Mail's compose page loads a reply's thread with `Email/get` and `Thread/get` (on `feat/send`). The inbox does not group by thread yet (Mail #25). Threads stay inside one account. |
| `SearchSnippet/get` | No plan | Search is local today. |

### Identity and EmailSubmission

| Method | Status | Notes |
| --- | --- | --- |
| `Identity/get` | Used | Reads `email` only, to list sending addresses under each account. Needs submission on the session and the mail account. Wildcard identities such as `*@example.org` are kept. |
| `Identity/get` for the From field | In progress | On Mail `feat/send`, compose loads every account's identities, with `textSignature` and `htmlSignature`, for the Send-from dropdown. |
| `Identity/set` (names, signatures, reply-to) | Planned | Signatures live on the server identity: one per identity, as `htmlSignature` and `textSignature`. Settings › Signatures on Mail `feat/send` will edit and copy them. |
| `EmailSubmission/set` | In progress | Mail `feat/send` sends a draft with no envelope and files it in Sent through `onSuccessUpdateEmail`, after an Undo window on the device that the user sets, 15 seconds by default. Mail #26 still plans the offline queue. |
| Delayed send (`maxDelayedSend`, `HOLDUNTIL`, `undoStatus`) | In progress | Schedule send on Mail `feat/send`, hidden when `maxDelayedSend` is 0. Undo cancels with `undoStatus: canceled`. |
| `EmailSubmission/get` | No plan | |

### Sieve (rules)

| Feature | Status | Notes |
| --- | --- | --- |
| `SieveScript/get`, `SieveScript/set` | Used | One script named "Otus Mail" per account, uploaded as a blob. |
| `onSuccessActivateScript`, `onSuccessDeactivateScript` | Used | Running and pausing rules. A server runs one script at a time. |
| `sieveExtensions` | Used | Gates conditions: `regex`, `mime`, `body`, `imap4flags`, `include`. |
| `SieveScript/validate` | No plan | |
| Raw Sieve editing | No plan | Decided against. Rules are edited only through the rule editor. |
| ManageSieve (RFC 5804) | No plan | Listed in the folders guide as an option for IMAP in v2. |
| Rules from reusable filters | Planned | [Mail #24](https://github.com/reazndev/otus-mail/issues/24). |

### VacationResponse

No plan. Otus does not read or write `VacationResponse`. Stalwart stores the vacation reply as a
Sieve script named `vacation`, so turning on Otus rules turns that reply off, and the rules page
warns first. An out-of-office setting needs its own issue and a decision on how it coexists with
the rules script.

## Contacts

| Method | Status | Notes |
| --- | --- | --- |
| `AddressBook/get` | Used | Lists books for the photo lookup. |
| `ContactCard/query` with `email` filter | Used | Exact address match, per book. |
| `ContactCard/query` with `text` filter | In progress | Recipient suggestions while typing, on Mail `feat/send`. |
| `ContactCard/get` | Used | Properties: `id`, `addressBookIds`, `emails`, `media`. Photos come from a blob or a data URI. |
| `ContactCard/set`, `AddressBook/set` | Planned | [Mail #8](https://github.com/reazndev/otus-mail/issues/8), [Mail #6](https://github.com/reazndev/otus-mail/issues/6), [Mail #19](https://github.com/reazndev/otus-mail/issues/19). |
| Recipient and attendee suggestions | Planned | Mail #26 and [Calendar #7](https://github.com/reazndev/otus-calendar/issues/7). Compose recipients are plain text today. |
| `ContactCard/changes` | Planned | Mail #19. |

The contact photo lookup is the only contact feature. It does not sync or store contacts.

## Calendars

Nothing is implemented. Calendar shows its welcome screen and has no JMAP code. The plan, in the
[Calendar roadmap](https://github.com/reazndev/otus-calendar/issues/1):

- Account setup through the shared accounts module ([Calendar #2](https://github.com/reazndev/otus-calendar/issues/2)).
- `Calendar` and `CalendarEvent` read and write, recurrence, time zones, reminders ([Calendar #3](https://github.com/reazndev/otus-calendar/issues/3)).
- Shared calendars, rights, and free/busy where the server supports them (Calendar #9).
- Invitations found in mail and answered from either app ([Calendar #5](https://github.com/reazndev/otus-calendar/issues/5), [Mail #31](https://github.com/reazndev/otus-mail/issues/31)).

The calendars spec is still a draft. Mail #12 pins the revision to the one the tested server
implements. Reminders stay local to the device and are not server alarms.

## Push and sync

| Feature | Status | Notes |
| --- | --- | --- |
| Push (`EventSource`, `PushSubscription`) | Planned | [Mail #18](https://github.com/reazndev/otus-mail/issues/18), [decision 0015](../decisions/0015-push-notifications.md): background check, UnifiedPush, Google FCM, and an experimental always connected mode, with no Otus server. The library has the `PushSubscription` wire types; no app uses them yet. |
| Local database and offline queue | Planned | [Mail #14](https://github.com/reazndev/otus-mail/issues/14), [Mail #16](https://github.com/reazndev/otus-mail/issues/16). Today nothing is cached. Each launch and refresh reloads from the server. |

Until sync lands, a screen cannot show data that was not loaded in this session, and there is no
offline mode. Design empty, loading, and error states for every account on its own, because each
login loads in parallel and fails alone.

## Settings: local or on the server

Use this when you add a setting. A setting that changes server data affects every mail app the
user has. A local setting affects only this install.

| Setting | Stored | JMAP effect |
| --- | --- | --- |
| Accounts | Device (protected storage) | Login only. |
| Folders | Server | `Mailbox/set`. |
| Rules | Server | `SieveScript/set`. |
| Read later switch | Device | Turning it on creates a server folder. Turning it off leaves the folder. |
| Combined folders | Device | None. |
| Swipe actions, mark read on open | Device | None. The actions they trigger change keywords and folders. |
| Inbox pills | Device | None. |
| Date format | Device | None. |
| Avatar options (domain icons, external photos) | Device | None. Public image requests send no account credentials. |

No setting syncs between devices or between Mail and Calendar. Export and import of settings is
planned as a JSON file ([Mail #20](https://github.com/reazndev/otus-mail/issues/20)), not through
JMAP.

## Updating this guide

Update the status of a row in the same change that adds, removes, or ships the feature. Move a
row from "In progress" to "Used" only when the branch merges to `main`.
