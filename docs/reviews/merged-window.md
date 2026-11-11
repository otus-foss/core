# Daily review and fix prompt

Review all changes merged into the selected Otus target branches during the last 24 hours.
Find regressions introduced by those changes, including failures caused by their combination.
Check which problems still exist at the frozen target revisions. Create GitHub issues for confirmed
actionable defects, implement focused fixes, and open pull requests. Run unattended using the
defaults below. This prompt authorizes those issue, branch, commit, push, and PR operations on each
run, including necessary updates to automation-owned issues and PRs. Do not ask again for approval
of those steps. Leave merges, releases, deployments, and direct pushes to target branches to the user.

Read `review-guide.md` beside this prompt and follow its evidence standards and engineering checks.
This prompt's fix workflow is the explicit exception to that guide's review-only contract. Read the
current repository instructions before editing. If the guide is unavailable, report the missing
instructions and incomplete coverage.

## Fixed defaults

```text
SCOPE: both
LOOKBACK_HOURS: 24
END_UTC: capture the current UTC time at the start of this run
TARGET_BRANCHES: discover the remote default branch of each selected repository
VALIDATION: full
REPORT_DIR: ~/.local/state/otus-reviews/daily/runs
STATE_FILE: ~/.local/state/otus-reviews/daily/<SCOPE>/state.json
LOCK_FILE: ~/.local/state/otus-reviews/daily/run.lock
WORKTREE_DIR: ~/.local/state/otus-reviews/daily/worktrees
```

No input values need editing between runs. The default repositories are `otus-foss/otus-core` and
`otus-foss/otus-mail`, with their sibling checkouts in this workspace. An explicit `SCOPE: meta` or
`SCOPE: mail` limits a schedule to one repository. Explicit repository/state directory overrides
are allowed for another machine; the rolling window remains 24 hours. `<SCOPE>` in `STATE_FILE` is
the run's scope, so `both`, `meta`, and `mail` schedules each keep their own checkpoint, such as
`daily/meta/state.json`. One scope's run never reads, archives, or re-baselines another scope's
checkpoint. All scopes share `LOCK_FILE`, because their repository work can overlap.

Calendar is always excluded. Expand `~`, create the local state directories, and keep worktrees,
reports, and state outside the repositories. Use the scheduler's existing GitHub credentials.
Verify read access and issue/branch/PR write access before mutations. Missing credentials or
permissions produce a blocked report, not an interactive login or a request for secrets in chat.
Continue independent read-only work where possible. Do not create or change the schedule itself.

Acquire a nonblocking exclusive lock before mutations or checkpoint changes and hold it for the
whole run. A lock that ends after one shell command is insufficient. If another run owns it,
record a skipped run and stop without changing shared state or GitHub. Release the lock on exit;
use process ownership so a crashed run does not leave a permanent lock. Recover prior incomplete
work from state and GitHub before creating anything new.

## Freeze time and revisions

1. Capture `END_UTC` once at the beginning. Set `START_UTC` to that instant minus exactly 24 hours.
   Use the half-open UTC interval `START_UTC <= mergedAt < END_UTC`. Preserve full timestamp
   precision and report the bounds. Do not use local dates or daylight-saving arithmetic.
2. Read the checkpoint before selection. Validate its schema, repository/branch identity,
   review mode, previous coverage end, and previous target SHAs. Read only this scope's
   `STATE_FILE`. Do not silently reuse state from another branch or an incompatible format, and
   treat a scope field that does not match the file's path as corruption. A corrupt or
   incompatible checkpoint is a coverage gap. Archive the original before recovery, reconcile saved manifests and GitHub records, and
   establish a fresh baseline if recovery is impossible. Report any lost historical coverage.
3. Always select the last 24 hours. Also resume unfinished reviews/fixes and catch up from a prior
   successful checkpoint when a run was missed. List older catch-up work separately from the daily
   window. Reconcile the two hours before the previous checkpoint's end as an overlap for delayed
   API indexing; deduplicate already reviewed changes. Record the overlap and catch-up bounds. Never
   move the successful checkpoint backward or silently discard pending work as it ages out.
4. Fetch the target refs without altering user work. Freeze their full SHAs and the observed UTC
   time. Record these separately from the selection window. Changes can land between `END_UTC`
   and the fetch. The frozen target is current-state evidence, not automatically the exact tree
   at `END_UTC`.
5. Never infer exact branch state at a wall-clock instant solely from author/committer dates.
   If a selected merge is absent from a fetched snapshot, reconcile once and freeze a new
   snapshot, or mark the scope incomplete. Label later changes used as current-state evidence.

## Select every merged PR

Use GitHub's actual merge metadata as the primary time source, not commit author time, push time,
`updatedAt`, or the presence of a merge commit. Include merge-commit, squash, and rebase merges.

1. Query each selected repository for merged PRs targeting each selected branch over a candidate
   interval that covers the effective window. Fetch all pages and record the total count.
   Apply the exact timestamp bounds locally using `mergedAt`, because search may use coarser or
   inclusive bounds. Deduplicate by repository and PR number.
2. Detect pagination/search caps and truncated results. Split the candidate time interval or use
   a paginated API that can enumerate all matches. Reconcile results with available target history
   or event evidence. Search indexing delay is a reason to use overlapping future windows and
   to disclose uncertainty near the end, never a reason to assert an empty result is complete.
3. Record each PR's URL, title, base branch, `mergedAt`, source head SHA if available, and landed
   SHA or range. Fetch changed files, PR commits, acceptance criteria, and relevant review threads.
   A PR merged into a feature branch is not a merge into the default branch. If a later parent PR
   brings it into the target, review that landed change without counting it twice.
4. Reconstruct each landed change from Git history and PR metadata. For a merge commit, compare
   it with its first parent to include conflict resolutions. For a squash, compare the squash
   commit with its parent. For a rebase, establish the actual target-branch range and its prior
   parent using history and API associations. Original PR SHAs may have been rewritten. If the
   mapping is ambiguous, retain the PR diff as partial evidence and flag the missing landed range.
5. Verify the landed commits are reachable from the frozen target, accounting for documented
   history rewrites. Keep a ledger of reviewed landed commits so nested merges or overlapping
   PRs do not cause duplicate review or findings. Do not use `git log --merges` as the selector.
6. Register selected PRs with the thread's PR-linking tool when available. Preserve the merged
   PRs. Put actionable findings in deduplicated issues and fix PRs through the workflow below.

## Reconcile direct pushes and other target changes

The user wants a daily branch checkup, so separately account for changes that bypass PRs. These
must not disappear merely because the merged-PR query returned nothing.

- With a compatible previous target SHA that is still an ancestor, compare its reachable history
  with the frozen target. Map commits to landed PRs and identify remaining branch changes. Exclude
  already reviewed commits and distinguish merges outside this window from unattributed changes.
  Review newly observed unattributed changes as an additional catch-up set.
- Use reliable push/event evidence for landing time when available. Without it, say "observed
  since the previous checkpoint; landing time unknown". Do not assign those commits to the exact
  hourly window based on author or committer timestamps. A late-authored commit can land much later.
- On the first run, reconcile the selected PRs with available history/events, but do not claim
  complete direct-push coverage without a reliable prior branch snapshot. Report the missing
  baseline. Establish the first checkpoint only for the frozen state actually inspected; label
  earlier unattributed history as unreviewed. Do not turn the run into an audit of all history.
- If the prior SHA is no longer an ancestor, investigate a force push or history rewrite. Report
  it and the affected coverage. Never silently replace the old checkpoint with a clean result.
- Keep late landings after `END_UTC` outside the merge-window selection. They may be relevant to
  current-state interactions or checkpoint reconciliation; label and account for them explicitly.

## Review the changes and their combined effect

For each selected landed change, apply the PR review method: read the complete changed functions,
callers, tests, affected platform implementations, and applicable shared-guide categories. Compare
with the prior target tree to establish a regression. Review merge resolution changes too.

Then inspect the combined result at the frozen targets. Look for assumptions invalidated by
another merge: Core APIs and Mail callers, source versus published dependencies, schema and cache
changes, overlapping state owners, renamed resources, theme/motion contracts, background workers,
and workflows that select a different Core branch. Run tests against the recorded combination.

Use time order within each target's ancestry. For independent repositories, merge timestamps help
describe the sequence but do not prove that an intermediate Core/Mail combination was deployed.
Use CI/nightly artifact provenance when claiming that users received a broken combination.

Check a suspected defect at introduction and again at the frozen current targets. Classify it as:

- `still present`: the frozen current code retains the defect.
- `fixed later`: a later change removes it; cite that change.
- `reverted`: a later change undoes the affected behavior; cite the revert and verify its effect.
- `unverified`: the landed mapping or current behavior cannot be established.

Keep fixed/reverted problems in a short historical section rather than the active action list.
Mention possible lasting effects, such as a data migration, only with evidence. Attribute
interaction defects to all relevant changes and explain why they fail together. Put unrelated
pre-existing defects outside the new-regression list.

If prior finding records exist, distinguish new, still-open, resolved, and recurring findings by a
stable fingerprint based on repository, logical symbol/path, and root cause. Do not use line numbers
or severity alone as identity. Recheck a prior finding before calling it resolved. If it was not
rechecked, mark it `not rechecked`; absence from today's diff is not evidence of a fix. A duplicate
finding need not repeat its full explanation, but retain its ID, status, and evidence link.

On every run, reconcile pending automation-owned PRs even when today's window contains no changes.
After a PR merges, verify the target contains the correction before resolving its finding. Remove
an automation-created worktree only when its recorded work is complete, its tree is clean, and
all commits are preserved remotely. Keep pending or unexpected work intact and retain the report
and ownership records. Do not let abandoned review worktrees accumulate between runs.

## Create issues without duplicates

For each confirmed, still-present actionable defect, create or reuse an issue in the repository
that owns the correction. Handle the highest-severity findings first, then continue through the
remaining findings. Do not file speculative concerns, accepted tradeoffs, cosmetic preferences,
already fixed/reverted defects, or routine reports with no findings.

1. Derive a stable fingerprint from repository, logical code location, and root cause. Store the
   components and a deterministic digest, without timestamps or line numbers. Include a hidden
   marker such as `<!-- otus-daily-finding:v1:<digest> -->` in automation-created issues and PRs.
2. Check local records and search both open and closed issues/PRs for the marker and equivalent
   problem descriptions. A human-filed equivalent counts as an existing issue. Inspect matching
   bodies and linked fixes; matching titles alone are insufficient. Paginate results. If access
   fails, defer creation rather than risk a duplicate.
3. Reuse an existing issue and its open fix PR. Add a comment only for new evidence, a changed
   status, or a new blocker; do not post the same daily reminder. Do not rewrite human-owned
   descriptions or push to human-owned branches. Respect a maintainer's explicit rejection or
   decision to defer a change unless a new regression materially changes the evidence.
4. For a new issue, include severity, trigger, expected/actual behavior, impact, exact source
   locations/SHAs, introducing PRs, reproduction or source trace, correction direction, and
   acceptance checks. Link the eventual fix PR. Use existing labels only when they fit.
5. For an interaction across repositories, create one issue per repository only if each needs
   a distinct change. Link the issues and state the order. Never create a Calendar issue.
6. Persist the returned issue URL immediately. If a create request times out or its response is
   lost, search for its marker before retrying. Apply the same rule to PR creation and updates.

## Implement and open fix PRs

This is an execution task. Do not stop at a proposed fix when the evidence supports implementing it.
Work through confirmed findings and pending automation-owned fixes, keeping unrelated corrections
in separate PRs. A focused fix can cover multiple findings with the same root cause.

1. Re-read the latest remote target and confirm the defect still exists before editing. Record
   the fix base SHA separately from the frozen review SHA. If another change fixed it, update
   the finding's evidence and avoid a redundant PR. Close an automation-created issue only after
   verifying the fix landed; do not close human-owned issues automatically.
2. Use an isolated worktree and a stable branch such as `fix/daily-<fingerprint-short>`. Reuse
   automation-owned worktrees/branches only after checking their recorded ownership, current
   remote SHA, and dirty state. Never discard unexpected edits. Enable the versioned Git hooks
   in new checkouts. Preserve the user's main checkout and other sessions' work.
3. Inspect existing code and apply the smallest complete correction that preserves supported
   behavior and repository boundaries. Fix a shared visual component in Core. Do not work around
   it by drawing in Mail. Update affected docs and Core's changelog for public behavior/API changes.
   Avoid speculative abstractions and unrelated cleanup.
4. Add or extend a behavior/regression test that exposes the original failure and passes with
   the correction. Check the failure against the base when practical. Use fake data and existing
   fixtures. Validate concurrency, cancellation, persistence, or partial failures when they caused
   the bug. For visible changes, follow the screenshot rules and attach the resulting captures.
5. Inspect the final diff yourself against the shared guide. Run the repository's formatting,
   focused checks, and `./gradlew checkAll` after the complete batch of edits. Run applicable
   additional platform checks. Use one Gradle command at a time and preserve the build queue.
   Do not reuse a check result from the review SHA as proof of the fix SHA. Do not weaken gates,
   skip hooks, or report an unrun check as passed.
6. Commit with `type(scope): imperative action`, a blank line, and 1 to 8 explanatory bullets.
   Push only the fix branch, then create the PR as a draft. The description must explain the
   concrete trigger, resulting behavior, issue link, regression test, exact validation results,
   remaining gaps, and any dependency on another PR. Use a body file or structured tool input
   so Markdown and newlines survive shell quoting. Record the head SHA and returned URL.
7. Register every created or updated PR with the thread's PR-linking tool immediately, including
   every layer of a stack. Before finishing, reconcile the thread's linked PR list. Report a
   linking failure accurately without repeating PR creation.
8. Mark a draft ready only after all intended commits are pushed, required checks passed for
   that revision, and dependencies permit review. This starts Otus's one-time PR CI. Keep drafts
   for incomplete checks, unresolved dependencies, or unfinished fixes and name the blocker.
   If later commits need fresh CI, follow the current workflow's manual rerun path; never imply
   the earlier green result covers them. Do not toggle draft/ready repeatedly to trigger runs.
9. Reconcile an existing automation-owned PR before adding commits. Check CI, review feedback,
   current base/head, and whether someone else changed the branch. Address concrete feedback and
   fix regressions within the same task. Merge the updated base when needed and safe; do not
   force-push or rewrite another contributor's work. Preserve conflicts that require a product
   decision as an explicit blocker. Never create a second PR merely because the first is pending.
10. Never merge a PR, enable auto-merge, push a release tag, publish packages, deploy, or push to
    a default/target branch. The output of this workflow is reviewable issues and fix PRs.

For a coordinated Core/Mail fix, prepare both changes in separate repositories and link both PRs.
Use a matching branch name when current Mail CI resolves Core that way. Validate the exact pair
and the intended consuming path. Do not invent a published version, bypass repository policy, or
silently rely on a local composite. If a release or an unresolved compatibility decision is needed,
keep the dependent PR in draft and state the dependency. Do not adopt Calendar as extra work.

When a fix needs a product decision or broader changes than the evidence supports, leave a concrete
issue with the unresolved decision and continue other findings. If a tested fix cannot be completed,
preserve useful isolated work and record the next step. An already useful implementation may be
pushed as a draft after the required checks have been attempted, with failures and missing platform
checks stated plainly. Never mark it ready or label it validated. Do not publish placeholder PRs
containing only a plan. Retry transient blockers on later runs; record unresolved decisions without
asking a question that stalls the unattended run.

## Validate and produce the report

Run focused checks and full validation at the frozen review revisions, as the shared guide
specifies. Do not run `checkAll` separately for every reviewed merged PR. Run it once per selected
repository with review work. Each changed fix branch also needs its own final validation as above.
For paired work, name the Core/Mail SHAs and resolution mode used. Explain any intermediate revision
tested to isolate an introduction. Pending CI may be reported as pending; the next scheduled run
reconciles it. Do not busy-poll PRs or hold this run open waiting for human review.

Return the shared-guide report with these daily details:

- Requested/effective UTC bounds, scope, target branches, frozen SHAs, and snapshot times.
- Selection/coverage status separate from the defect verdict: `complete`, `partial`, or `failed`,
  with merged-PR coverage and direct-push coverage stated separately. Include the validation mode.
- A change inventory with each PR or unattributed change, landing evidence, files/areas, and
  review status. State counts discovered, reviewed, skipped, and unresolved; explain discrepancies.
- Active findings first, in severity order, with introducing PR/commit and present-day evidence.
  Then fixed/reverted findings, previous findings' status, interaction risks, and validation gaps.
- Issues created/reused, fix branches and head SHAs, PR URLs, draft/ready status, checks and review
  status, and blockers. Distinguish `fix proposed` from `fixed on target`; opening a PR is not a fix
  in the app. Include resumed work and the next action for every unfinished correction.
- Commands/results, CI freshness, dependency combinations, unchecked platforms, rate limits,
  missing history, unavailable repositories, and any work needed to finish the review.

If selection is verified empty, say "No merged PRs in the last 24 hours" and give both UTC bounds.
Still reconcile and resume pending issues/PRs from prior runs. Say whether direct-push reconciliation
found work or remains unverified. Do not claim
the repositories are healthy when there was no review work. API/auth/history failure must produce
an incomplete report, not "no changes". Continue reviewing any independently accessible repository.

## Save recurring-run evidence

Write a uniquely named Markdown report and JSON manifest in `REPORT_DIR` on every run.
Include these fields in the manifest:

```text
schema_version, run_id, started_at_utc, finished_at_utc
requested_window, effective_window, scope, validation_mode
repositories: repository, branch, target_sha, snapshot_at_utc, dependency_revisions
selection: merged_pr_status, direct_push_status, pages_complete, gaps
changes: repository, pr_url, merged_at, landed_shas, mapping_status, review_status
findings: id, fingerprint, severity, confidence, lifecycle_status, evidence, introduced_by
actions: fingerprint, issue_url, branch, worktree, fix_base_sha, fix_head_sha, pr_url, status, blocker
checks: repository, sha, dependency_mode, command, outcome, report_path
coverage_status, verdict, report_path
```

Use arrays or objects for grouped fields and JSON null for unavailable values. Include concrete
reasons for missing evidence. Save the report and manifest even when the run is incomplete.

Maintain two kinds of state independently: review coverage and the issue/fix backlog. Persist
remote side effects and pending work immediately, even if the run later fails. Before creating
an issue or PR, record the intended fingerprint/branch; after creation, record its URL. Reconcile
unfinished operations with GitHub on the next run. This prevents a crash between remote creation
and checkpoint completion from creating duplicates.

Advance the successful review checkpoint atomically only after saving the evidence. Store
schema version, repository/branch identity, validation mode, completed window end, reviewed target
SHAs, reviewed change IDs, finding records, coverage limitations, and report/manifest paths. Never
advance coverage past unreviewed selected changes, unresolved history mappings, or required
validation that failed to run. A confirmed code defect with a completed review can advance coverage;
a blocked test cannot be recorded as completed validation. Record actual test failures faithfully.
Open issues or unmerged fix PRs do not prevent completed review coverage from advancing. They stay
in the backlog and receive attention on the next run regardless of their age. Retain fix ownership,
issue/PR URLs, branch and base/head SHAs, validation, blockers, and last processed review feedback.

Advance a target SHA only when all newly reachable changes since its prior SHA were accounted for,
including landings after the requested end. Otherwise retain the earlier SHA and record the pending
changes. On a first run, mark the new baseline explicitly as having no prior direct-push coverage.
For a combined review, retain the prior successful shared checkpoint if either repository remains
incomplete; the report can still record completed work for deduplication on the next attempt.

If another run changed the checkpoint since this run read it, preserve both reports and do not
overwrite the newer state. Report the collision. Checkpoints must not cause a scheduled run to
silently skip changes. If state storage is unavailable, use GitHub to reconcile prior side effects
and report the persistence failure. Do not create new remote work unless duplicate prevention can
still be established. Return paths to saved artifacts and links to every issue and fix PR handled.
