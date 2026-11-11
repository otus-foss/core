# Landing pull requests

A pull request merges when a maintainer adds the `ready to land` label. If open pull requests in
the other Otus repositories come from a branch with the same name, they are its partners, and the
whole group merges together, Core first, once every one of them is ready. The nightly build pairs
Mail `main` with Core `main`, so partners must not land hours apart.

Each repository's `land` workflow calls Core's `lander` workflow, which does the work.

## Rules

A group lands when every pull request in it:

- has the `ready to land` label, added by a user with the admin or maintain role
- has an `otus/land` approval on its latest commit
- is not a draft
- has at least one check on its latest commit, and every check and status passed
- has no review that requests changes
- has no merge conflicts

Until then, each pull request in the group gets one comment that lists what is missing, and the
lander updates that comment as the state changes. Pull requests without the label get no comment.

- **Approval follows the commit.** Adding the label sets the `otus/land` status on the head commit.
  A new push removes the label, so the new commit needs approval again.
- **Checks start themselves.** CI runs once per ready pull request. When the label goes on a commit
  that has no checks, the lander starts `quality` on the branch.
- **Merges are pinned.** Each merge passes the checked head commit, so GitHub refuses it if someone
  pushed in between.
- **Forks never pair.** A pull request from a fork lands alone, so nobody outside can attach a pull
  request to a maintainer's change. The workflows never check out pull request code, which keeps
  `pull_request_target` safe. A fork's own workflow files run its checks, so it can make them pass;
  read a fork's `.github/` changes before adding the label. A fork's app pull request always builds
  against Core's `main`, since its Core branch is not in otus-core.
- **A partial landing stops.** If Core merges and Mail then fails, the lander stops, says so in the
  comment, and fails its run. Merge the rest by hand.

Agents add `ready to land` only when the user asks them to land a pull request.

## Setup

The workflows use a GitHub App, because the default `GITHUB_TOKEN` cannot reach other repositories
and its merges do not start other workflows. Do this once:

1. Create a GitHub App under the `otus-foss` organization's Developer settings. Turn off the webhook. Give it these
   repository permissions: Actions, Contents, Issues, Pull requests, Commit statuses, and Workflows
   read and write; Checks read. Metadata read is added on its own.
2. Install it on `otus-core`, `otus-mail`, and `otus-calendar` only.
3. Add the App's client ID as the `LANDER_CLIENT_ID` organization variable and a private key as the
   `LANDER_APP_PRIVATE_KEY` organization secret, both for these three repositories.

The `land` workflows run from `main`, so they start working once this change merges.

## Public repositories

Rulesets need a public repository on this plan. Once a repository is public, add a ruleset for
`main` that blocks deletion and force pushes and requires the `otus/land` status, so the merge
button also needs an approval. Repository admins can bypass it for direct pushes and emergencies:

```sh
gh api -X POST repos/otus-foss/<repository>/rulesets --input - <<'JSON'
{
  "name": "main",
  "target": "branch",
  "enforcement": "active",
  "conditions": { "ref_name": { "include": ["~DEFAULT_BRANCH"], "exclude": [] } },
  "bypass_actors": [{ "actor_id": 5, "actor_type": "RepositoryRole", "bypass_mode": "always" }],
  "rules": [
    { "type": "deletion" },
    { "type": "non_fast_forward" },
    {
      "type": "required_status_checks",
      "parameters": {
        "strict_required_status_checks_policy": false,
        "required_status_checks": [{ "context": "otus/land" }]
      }
    }
  ]
}
JSON
```

Also set Settings, Actions, General, "Approval for running fork pull request workflows" to require
approval for all external contributors, so a fork's workflows wait until a maintainer starts them.
On the same page, set "Workflow permissions" to read-only and turn off "Allow GitHub Actions to
create and approve pull requests"; each workflow asks for the write access it needs.
