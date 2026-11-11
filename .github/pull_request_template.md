<!--
Title: type(scope): short action, with the commit title rules in AGENTS.md.
Open as a draft. Mark it ready once every commit is pushed, because CI runs once.
Delete a section that has nothing to say, except Testing.
-->

<!-- One or two sentences: what changes for the apps that use it, and why. -->

## Changes

<!--
One bullet per change, naming the public declarations it adds or changes.
Start a breaking change with "Breaking:" and say what each app must change.
Add each public API or behavior change under "## Unreleased" in CHANGELOG.md.
-->

-

## Depends on

<!-- The app PR that adopts this as otus-foss/mail#123, or the design page. -->

-

## Screenshots

<!--
Add images when the change is visual or a picture explains it faster than words.
Show each changed state in the themes where it looks different. Put a small change in a
Before | After table. Link a recorded screenshot at the head commit, which stays put after merge:
![Inbox](https://github.com/otus-foss/core/blob/<sha>/<path>.png?raw=true)
Drag phone captures and screen recordings into the description on GitHub.
-->

## Testing

<!-- Only what ran, with its result. -->

- Checks: <!-- `./gradlew checkAll` passed, or the narrower tasks that ran -->
- Tests: <!-- new or re-recorded tests and screenshots, and what each covers -->
- Device: <!-- the app build and phone you tried it on, or "Not run on a device." -->

## Not covered

<!-- Known gaps, risks, and follow-ups. -->

-

<!-- Closes #123, or Part of #123 -->
