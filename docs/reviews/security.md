# Security and protocol audit prompt

Audit the selected Otus repositories for security defects, JMAP specification violations, race
conditions, and code smells that hide bugs. Review implemented behavior at the chosen revisions,
regardless of when a problem was introduced. Produce a report and a prioritized correction plan;
do not implement changes.

Read `review-guide.md` beside this prompt and follow it throughout. If it is unavailable, report
that the Otus review instructions are missing before claiming a complete audit. This prompt goes
deeper than [repository.md](repository.md) in four areas and skips its UI, theme, and motion
checks unless they carry a security or data-loss risk.

## Inputs

```text
SCOPE: meta | mail | both
REFS: remote default branch for each selected repository, or explicit refs/SHAs per repository
VALIDATION: full | targeted | static
REPO_PATHS: optional explicit paths for Meta and Mail
FOCUS: optional subset of: security, jmap, races, smells (default: all four)
AGENTS: optional maximum number of parallel review agents (default: 3)
REPORT_DIR: optional directory outside the repositories
```

Default to `SCOPE: both`, remote default branches, and `VALIDATION: targeted`. Resolve branch
names from the remotes and freeze each ref to a full SHA. Review a detached worktree of that SHA,
never the user's checkout, so uncommitted work stays out of scope. Calendar is always excluded.

## Plan the audit

1. Establish the shared guide's evidence. Inventory modules, source sets, platform entry points,
   storage schemas, network clients, background workers, exported Android components, the iOS
   push extension, and Mail's `push_bridge/`.
2. Build a threat map before reading code in depth. List each trust boundary, what crosses it,
   and who controls the input:
   - The JMAP server and anything it returns, including mail bodies, headers, blob content,
     mailbox names, capability objects, URLs, and error bodies.
   - Mail senders, who control message content but not the server.
   - Push services and the push bridge, which see endpoints and ciphertext.
   - Other apps on the device, through intents, content URIs, shared files, and the clipboard.
   - Public avatar, icon, and BIMI hosts, which learn which addresses the user looks up.
   - The update channel that installs nightly builds.
3. Read Meta's `docs/jmap.md` security table, `docs/push.md`, `docs/avatar.md`, and the accepted
   decisions that set trust rules. Earlier review fixes, such as the blob origin checks from the
   performance and security review, are claims to verify, not settled facts.
4. Split the work by area when `AGENTS` allows more than one. Give each agent one area below,
   the frozen SHAs, the worktree paths, the threat map, and the finding format. Agents read and
   run focused checks only; one Gradle command at a time on this machine. Merge their findings,
   remove duplicates, and verify every finding yourself before it enters the report.

## Security

Trace each boundary from the threat map to the code that enforces it. Look for:

- Credentials and tokens: storage on both platforms, backup exclusion, logging, crash reports,
  exception messages, `toString()` of data classes, and debug builds. Credentials must reach only
  the entered origin and origins the user approved, never an unapproved origin, avatar host, or
  push endpoint.
- Transport: HTTPS enforcement, redirect handling, cleartext exceptions in the network security
  config, certificate handling, and whether a session URL, `apiUrl`, `downloadUrl`, `uploadUrl`,
  or `eventSourceUrl` can point the client at another host.
- Received content: HTML sanitizing and WebView settings, JavaScript, file and content access,
  remote image loading and its consent, `cid:` resolution across messages or accounts, link
  schemes such as `intent:`, `javascript:`, and `file:`, and display names or subjects that spoof
  another sender or break layout with control or bidi characters.
- Files: attachment and download paths, file-name sanitizing against path traversal, content URI
  grants, `FileProvider` paths, temporary file cleanup, and MIME types taken from the server.
- Android surface: exported activities, services, and receivers, pending intent mutability,
  deep links, notification actions that act without unlocking, and permissions the app requests.
- Push: VAPID and encryption keys, subscription ownership, verification codes, replay, and what
  the bridge or notification shows on a locked screen.
- Update install: how the app finds, downloads, and verifies a nightly build before installing it.
- Resource limits: response size, decompression, image decoding, recursion depth in MIME or
  JSON parsing, and regexes that a sender can make slow.

For each security finding, show the input, the boundary it crosses, the reachable operation, and
the impact, as the shared guide requires. Use a local reproducer, never a live server.

## JMAP conformance

Check behavior against the RFC text, not against what one server accepts. Cite the section for
every finding. Fetch the RFC if it is not available locally and say so if you cannot.

- RFC 8620 core: session resource fields, capability checks before use, `using` lists, account
  selection by capability, request limits from `maxCallsInRequest`, `maxObjectsInGet`, and
  `maxObjectsInSet`, result references, creation IDs, `/changes` with `hasMoreChanges`,
  `/queryChanges`, `cannotCalculateChanges`, `stateMismatch`, `ifInState`, method-level and
  request-level errors, and push `StateChange` and `PushSubscription` handling.
- RFC 8621 mail: `Mailbox` roles and rights, `Email` property names and `header:` forms,
  `keywords` and `mailboxIds` patch paths, `Email/import`, `Email/parse`, `EmailSubmission` with
  `onSuccessUpdateEmail` and `onSuccessDestroyEmail`, `Identity` use, `Thread` membership,
  `SearchSnippet`, and `VacationResponse`.
- RFC 9610, RFC 9553, RFC 9661, and RFC 9670 where Otus uses contacts, JSContact, Sieve, or
  sharing. Check that patches leave fields the client does not understand untouched.
- Serialization: omitted versus `null`, unknown properties preserved or ignored, JSON pointer
  escaping of `~` and `/` in patch keys, `UnsignedInt` and `UTCDate` ranges, and IDs treated as
  opaque strings.
- Partial failure: every `/set` reply can contain `notCreated`, `notUpdated`, and
  `notDestroyed`. Find callers that treat the method response as success without reading them.
- Provider assumptions: Stalwart-only behavior in common code paths, and features used without
  checking the capability first.

## Race conditions and lost updates

Look for interleavings that lose user data, show stale state, or send twice:

- Two writers to the same state: optimistic UI updates and the server reply, a refresh landing
  after a newer local change, sync and push arriving together, and two screens editing the same
  draft or settings.
- Stale completions: a request that finishes after its account was removed, switched, or signed
  in again with the same ID, and results applied to a list that has since changed.
- Coroutine misuse: `GlobalScope`, scopes that outlive their owner, swallowed
  `CancellationException`, `runCatching` around suspend calls, unstructured `launch` without
  error handling, and shared mutable state without a `Mutex` or single-writer rule.
- State tokens: a `/changes` or `/queryChanges` state saved before its changes are applied, or
  applied without saving, so a crash replays or skips changes.
- Outbox and send: a retry after a lost reply that sends the message again, a draft destroyed
  before submission succeeds, and undo or cancel racing the real send.
- Process death and storage: SQLDelight writes outside a transaction that must be atomic,
  WorkManager unique-work policies that drop or duplicate work, and in-memory queues that vanish.
- Lost conditions: `when` and `if` chains that miss a case, `else` branches that hide a new enum
  value or JMAP error type, early returns that skip cleanup, and checks done once that later code
  assumes still hold.

Prove a race with a specific interleaving and the code lines on each side. Prefer a deterministic
test with a test dispatcher or controlled mock server over a timing-based reproducer.

## Code smells that hide bugs

Report a smell only when it causes or is likely to cause a concrete defect, or breaks a rule in
the repository's `AGENTS.md` or `docs/quality.md`. Name the consequence.

- Errors swallowed by empty `catch`, `catch (e: Exception)` around wide blocks, `getOrNull()`
  without a fallback the user can see, or logs in place of handling.
- Non-null assertions (`!!`), unchecked casts, and `first()` or `single()` on server data.
- Duplicate rules that already diverge, or will, such as two copies of origin checks, mailbox role
  logic, or address parsing.
- Stringly typed IDs mixed between accounts, mailboxes, emails, and identities.
- Hard-coded hosts, ports, limits, or timeouts that should come from the session or policy.
- Dead code, unused parameters, blanket suppressions, and `TODO` markers that disable a check.
- Files near the line limits whose split would separate a security check from its caller.

## Validate and report

Challenge each candidate with existing guards, callers, tests, and accepted decisions before
reporting it. In targeted mode, run focused tests that cover the claimed path, such as one test
class with `--tests`, and record the full gate as not run. In full mode, also run `./gradlew
checkAll` once per reviewed repository. Never change production code to make a check pass.

The report follows the shared guide's format, with these additions:

- Group findings under Security, JMAP conformance, Race conditions, and Code smells, each in
  severity order. Give every finding a stable ID such as `SEC-1`, `JMAP-1`, `RACE-1`, `SMELL-1`.
- Include the threat map and say which boundaries the audit covered fully, partly, or not at all.
- For JMAP findings, quote the RFC section number and the requirement level (MUST, SHOULD, MAY).
- End with a correction plan that names the owning repository and orders Meta changes before the
  Mail changes that depend on them.

If `REPORT_DIR` is supplied, save the Markdown report there with a UTC timestamp and scope in its
name. Otherwise return it in the conversation.
