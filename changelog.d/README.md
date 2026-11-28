# Changelog fragments

Each pull request that changes a module's public API or behavior adds one file here instead of
editing `CHANGELOG.md`, so pull requests never conflict over it. Name the file after the branch,
with `/` as `-`, such as `feat-simplify-inbox.md`, and write the entries as `CHANGELOG.md` would,
one bullet per API:

```markdown
- Design system: `EntrySize`, `Compact` or `Comfortable`, for entry cards.
```

A release runs `python3 tools/changelog.py <version>`, which moves every fragment into
`CHANGELOG.md` and deletes it; see [releasing](../docs/releasing.md).
