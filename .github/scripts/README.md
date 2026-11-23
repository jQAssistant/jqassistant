# Trivy vulnerability issues

The `Trivy vulnerability issues` workflow in `../workflows/trivy.yml` scans the
pushed commit on every push (including tags), and the default branch twice daily
at **06:17 and 18:17 UTC**. Scheduled runs start once the workflow is on the default
branch; GitHub may delay scheduled runs during periods of high load.

The official `aquasecurity/trivy-action` scans a generated dependency SBOM for
HIGH and CRITICAL dependency vulnerabilities, including vulnerabilities without a
published fix. Before scanning, the job sets up Temurin Java 17, restores the Maven
cache and runs `mvn --batch-mode --no-transfer-progress -PIT,it-store-file
-DskipTests=false -Dmaven.test.skip=false -DskipITs=false install` to build the
reactor, run unit and integration tests, install sibling modules and resolve
transitive dependencies. The `IT` profile enables Failsafe and Maven Invoker
tests; `it-store-file` selects an embedded store. The scan job has a 120-minute
timeout to accommodate the tests and scans. Test failures stop the scan job.

After the build, a separate CycloneDX `makeAggregateBom` invocation creates
`target/trivy-dependencies.json` with compile, runtime, provided, system and test
scopes included. Trivy uses this fresh aggregate CycloneDX JSON SBOM as its only
scan input. It covers the active Maven reactor and build profiles, including
direct and transitive test dependencies. Standalone fixtures and inactive
profiles outside that build are not automatically included.

The normal production SBOM configuration is unchanged. Module-specific files
such as `plugin/java/target/bom.json` describe only that module and exclude test
scope, so the workflow generates its own aggregate SBOM for repository-wide
coverage including tests.

Vulnerability database updates remain enabled. Secret scanning is disabled.
Trivy writes `trivy-report.json` directly for issue creation. The `trivy-report`
artifact contains this scan report and the dependency SBOM, and is retained for
14 days.

Maven caches are keyed by the repository's POM files. The build runs even on a
cache hit; build or dependency-resolution errors prevent both scanning and issue
creation. A cold cache still requires Maven repository access. If that access
returns HTTP 429, respect the server's retry delay or configure an accessible
repository mirror before rerunning. The SBOM scan itself uses the dependency
inventory from the JSON file and does not resolve source POMs.

The scan job has read access. The separate issue job uses the built-in
`GITHUB_TOKEN` with `issues: write`; no personal access token or additional secret
is needed. Repository/organization policy must allow the pinned actions and issue
creation, and repository Issues must be enabled. Scan errors prevent issue
creation; finding vulnerabilities does not fail the scan.

## Issue identity and lifecycle

Each issue has a hidden body marker:

```text
<!-- trivy:v4:SHA256_OF_JSON_ARRAY_TYPE_PACKAGE_ADVISORY -->
```

The hash is SHA-256 of `JSON.stringify([type, PkgName, VulnerabilityID])`.
Trivy's Java `jar` type (SBOM scanning) is normalized to `pom` (source
scanning), grouping the same package and advisory across scan modes.
Versions, severity, module paths and branches are excluded. The same finding in
multiple modules becomes one issue with affected locations, installed/fixed
versions, severity, commit, ref and a link to its scan run.

Only `trivy:v4` markers are recognized. Older `trivy:v1`, `trivy:v2` and `trivy:v3`
issues are ignored so the next scan can create fresh tickets for the current
SBOM findings included by the workflow's severity filter. Existing older issues
are left unchanged. Once a `v4` issue exists, subsequent scans skip that finding.

Every run paginates all open and closed issues and checks their markers. Renaming
an issue, changing its labels or closing it does not create a replacement.
Issues are left unchanged when a finding repeats or disappears; there is no
automatic update, reopening, commenting or closing. Manually created issues are
recognized only if their body contains the matching marker. Keep markers intact.

Only the issue job is serialized across all branches with `trivy-issue-sync`.
Scans remain independent, and up to 100 issue jobs can wait in the concurrency
queue. This prevents two workflow runs from creating the same issue concurrently.
Any other workflow that creates these issues must use the same concurrency group.
New issues are created serially with a one-second pause between writes to reduce
secondary rate limiting. API errors fail the run; rerunning lists existing issues
again before attempting any remaining creations.

## Local tests

With Node.js 24 installed, from the repository root:

```bash
node --test .github/scripts/trivy-issues.test.cjs
```

Tests mock the GitHub API and use fixed Trivy reports; they need no token, network,
Trivy installation or Java build. They cover repeated findings, grouping modules,
closed/renamed issues, identity stability, large issue lists, dry runs, API errors,
malformed/empty reports and report truncation. The workflow runs them before every
scan.

## Testing on GitHub

1. Push these files to a sandbox repository (a fork works). Enable Actions and
   Issues there. Push-triggered runs create real issues in that repository.
2. Open **Actions → Trivy vulnerability issues → Run workflow**. Leave
   **dry_run** enabled to scan and preview new issue titles in the logs and counts
   in the run summary. Confirm the Maven build succeeds before Trivy starts.
   Download `trivy-report` and check that the SBOM includes test dependencies and
   the expected transitive packages and
   HIGH/CRITICAL findings appear, including dom4j and Velocity if the resolved
   versions remain vulnerable. Confirm the artifact contains `trivy-report.json`
   and `dependency-sbom.json`. This preview does not create issues.
3. Run manually with **dry_run** disabled. Check that each new finding creates
   one issue with the expected package, advisory, remediation details and marker.
4. Rerun that workflow run. For unchanged findings, the issue count must remain
   unchanged. Rename and close an issue, then rerun: it must still be skipped.
   Fresh scans may report additional advisories as Trivy's database changes;
   fixed reports in the local tests isolate deduplication from database changes.
5. Start two manual runs close together with **dry_run** disabled. Confirm scans
   can overlap while their issue jobs run one at a time and create no duplicates.
6. Verify a new push triggers a scan. Put the workflow on the sandbox default
   branch and verify the next scheduled run as well.

For production, push the files and merge them into the default branch to enable
scheduled scans. Pushes and scheduled scans always create issues; the dry-run
input applies only to manual runs.
