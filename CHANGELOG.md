# Changelog

## Unreleased

## 0.2.1 - 2026-10-08

- Publish `com.otus:designsystem`, which failed in 0.2.0, together with the other modules. The
  code is unchanged from 0.2.0. See decision 0006.
- Build every publication before uploading any, and check publications in CI.

## 0.2.0 - 2026-10-08

Incomplete release: `jmap`, `dateformat`, and `avatar` were uploaded, `designsystem` was not. Use
0.2.1.

- Add `com.otus:dateformat`, `com.otus:avatar`, and `com.otus:designsystem`, moved from otus-mail.
  Packages change from `com.otus.mail.core.dateformat`, `com.otus.mail.core.avatar`, and
  `com.otus.mail.designsystem` to `com.otus.dateformat`, `com.otus.avatar`, and
  `com.otus.designsystem`.
- Make the panel, sheet, calendar, and page tokens, `panelTextStyle`, `controlPressFeedback`, and
  `EmptyAvatarStore` public, because Mail's features use them.
- Add `AvatarTokens.size` for the default avatar size, replacing Mail's `InboxTokens.avatarSize`
  in the shared `Avatar` element.

## 0.1.0 - 2026-10-08

- Add the repository build, quality gate, suite documentation, and publishing to GitHub Packages.
- Add `com.otus:jmap`, moved unchanged from otus-mail commit 507b5cb: the JMAP client, typed
  `/get`, `/query`, `/changes`, and `/set` calls, blob uploads and downloads, and login storage.
