---
title: "Public API and behavior changes need a changelog entry"
scope: "pull-request"
severity_min: "high"
---

## Instructions

Every public declaration in this repository is a contract with Otus Mail and Otus Calendar. Flag a
pull request that changes a module's public API or behavior without a line under `## Unreleased`
in `CHANGELOG.md`. A breaking change must name the affected declarations and the migration each
app needs. The version bump happens at release time, so do not ask for one in a feature pull
request.

Also flag these:

- Any reference to `com.otus.mail` or `com.otus.calendar`, including in tests.
- App-specific screens, navigation, notifications, credentials, or database instances added to a
  library module.
- A new module or public API that only one app needs, unless it is a design-system element,
  component, token, or symbol. Those always live here.
- A `gradle/libs.versions.toml` change that moves Kotlin past the version the apps use.
