# Repository review prompt

Review the selected Otus repositories in detail. Find existing defects, data/privacy risks,
broken user flows, and concrete architecture or delivery problems. Review implemented behavior
at the chosen revisions, regardless of when a problem was introduced. Produce a report and a
prioritized correction plan; do not implement changes.

Read `review-guide.md` beside this prompt and follow it throughout. If it is unavailable, report
that the Otus review instructions are missing before claiming a complete review.

## Inputs

```text
SCOPE: meta | mail | both
REFS: remote default branch for each selected repository, or explicit refs/SHAs per repository
VALIDATION: full | targeted | static
REPO_PATHS: optional explicit paths for Meta and Mail
FOCUS: optional areas needing extra depth, without dropping the rest of the requested scope
REPORT_DIR: optional directory outside the repositories
```

Default to `SCOPE: both`, remote default branches, and full validation. Resolve branch names from
the remotes rather than assuming `main`. Freeze each selected ref to a full SHA. Uncommitted
changes are excluded unless the user explicitly asks to review them. Calendar is always excluded.

## Map the repositories

1. Establish the shared guide's evidence and read the relevant docs. Inventory actual Gradle
   modules, source sets, feature packages, platform entry points, storage schemas, tests, build
   tooling, workflows, resources, and public library APIs at the frozen revisions.
2. Compare the inventory with the docs. Use source and callers to determine which behavior exists.
   Record documentation drift without treating documented future plans as requirements for today.
3. For Meta, inspect every existing module. Current starting points are `jmap`, `jscontact`,
   `jmap-contacts`, `push`, `avatar`, `dateformat`, `designsystem`, `accounts`, and `settings`.
   Discover additions or removals rather than treating this list as fixed.
4. For Mail, inspect app wiring, `core`, every implemented feature, Android integrations, iOS
   hosts/extensions, SQLDelight storage, `push_bridge`, build/update tooling, and tests. Feature
   guides such as storage, compose, folders, inbox, notifications, and Apple push provide context.
5. Record a coverage ledger with one row per module or feature group. Track its responsibilities,
   entry points/public contracts, high-risk paths, tests examined, and status: `reviewed`,
   `partial`, `not reviewed`, or `not applicable`. Briefly explain each partial or skipped area.

## Inspect behavior and boundaries

Apply every shared-guide category to the relevant code. Inspect each module's entry points and
contracts, then follow the paths that cross UI, domain, network, storage, and platform boundaries.
Read implementations and tests, not just directory names or architecture-check output.

Prioritize these paths, then continue through the remaining inventory:

- Meta's transport, authentication, capability handling, wire serialization, protected login
  storage, and public API compatibility. Check both platform implementations of security-sensitive
  contracts. Inspect contacts round trips, push lifecycle, avatar privacy, and date/locale behavior.
- Meta's design-system ownership, reusable control behavior, semantics, themes, motion, resources,
  and accounts/settings interfaces. Trace how Mail supplies state and responds to callbacks.
- Mail's startup, account lifecycle, cache/sync, inbox/search, folders/rules, read/message actions,
  compose/drafts/sending, attachments, notifications, settings, and updates, where implemented.
- Mail's Android lifecycle/background work, iOS bindings and push extension, and APNs bridge.
  Preserve Android priority while identifying demonstrable iOS regressions or missing validation.
- Dependency resolution, publication/release assumptions, nightly provenance, current CI coverage,
  hooks, and the actual checks behind `checkAll`.

For each important flow, consider a successful operation, invalid input, empty data, partial
server failure, slow or lost network, cancellation, concurrent operations, account removal,
storage failure, process recreation, and retry. Select cases that the implementation can actually
reach. Check large data sets and resource limits where input sizes are externally controlled.

Compare similar paths for differences that should not exist. Search for established components
and contracts before proposing extra abstractions. Report duplicated rules with specific copies
and a plausible divergence consequence. Identify dead code only after checking all callers,
platform source sets, resources, reflection/serialization use, and tests.

Use history when it explains a contract or suspected defect. It is not necessary to read every
historical commit. Confirm problems against the frozen current revision. Do not propose entire
missing products, speculative modules, or broad rewrites as review findings.

## Review Meta and Mail together

For `SCOPE: both`, explicitly inspect the boundary between them:

1. List the Meta artifacts Mail declares and the effective artifacts or source checkout it uses.
   Compare the catalog, Gradle settings, local overrides, and CI/nightly dependency selection.
2. Trace representative consumers of every used Meta module. Check source/API compatibility,
   behavior/default changes, account ownership, callbacks, resources, and theme/motion contracts.
3. Distinguish compatibility with the pinned published version from compatibility with Meta's
   reviewed source SHA. Test the relevant combination and disclose any unverified one. A
   build against a same-named Meta branch is a different result from a released-library build.
4. Check changelog and version policy against the public contract. Verify any claimed package
   availability when access permits; do not infer publication from a tag or version property.
5. Explain which repository should own each proposed correction and whether the other needs a
   coordinated change. Calendar compatibility stays unreviewed by explicit scope.

For a single-repository review, read the other repository only as needed to verify the selected
repository's contract. Do not expand the findings list into an unsolicited audit of that dependency.
If dependent source is unavailable, use the declared contract and report the coverage limitation.

## Validate and report

Challenge each candidate finding with existing guards, callers, tests, and accepted decisions.
Run the chosen validation mode and focused reproducers as the shared guide specifies. Diagnose
environment failures separately from source failures. Do not alter code to make checks pass.

The final report must contain:

- The shared-guide verdict and confirmed findings in severity order, with exact locations,
  triggers, impact, evidence, correction direction, and a regression check for each.
- A prioritized correction plan that references finding IDs, names the owning repository, and
  notes ordering where a Meta change must precede a Mail change. Keep optional improvements
  separate from defects and mandatory-policy violations.
- The coverage ledger, inspected revisions, effective dependency combinations, validation commands
  and results, documentation conflicts, and explicit unreviewed areas. A repository-wide scope
  is not a claim that every line or device state was exhaustively verified.
- The highest-value next check for each unresolved question. If the review ends before covering
  all modules, mark it incomplete and provide enough context to resume without repeating work.

Recheck remote refs and worktree status before finishing. Report drift without folding new commits
into the already completed review. If `REPORT_DIR` is supplied, save the Markdown report there with
a UTC timestamp and scope in its name, then return its path. Otherwise return it in the conversation.
