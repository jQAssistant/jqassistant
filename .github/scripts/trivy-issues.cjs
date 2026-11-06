const { createHash } = require('node:crypto');
const { setTimeout: delay } = require('node:timers/promises');

function isObject(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

function requireText(value, field) {
  if (typeof value !== 'string' || !value.trim()) {
    throw new Error(`Malformed Trivy report: ${field} must be a non-empty string`);
  }
  return value;
}

function optionalText(value, field, fallback) {
  if (value === undefined || value === '') return fallback;
  return requireText(value, field);
}

function collectFindings(report) {
  if (!isObject(report) || report.SchemaVersion !== 2 ||
      (report.Results !== undefined && !Array.isArray(report.Results))) {
    throw new Error('Unsupported or malformed Trivy report; expected SchemaVersion 2');
  }

  const findings = new Map();
  for (const result of report.Results || []) {
    if (!isObject(result) ||
        (result.Vulnerabilities !== undefined && !Array.isArray(result.Vulnerabilities))) {
      throw new Error('Malformed Trivy report: invalid result or vulnerabilities array');
    }
    for (const vulnerability of result.Vulnerabilities || []) {
      if (!isObject(vulnerability)) throw new Error('Malformed Trivy report: invalid vulnerability');
      const type = requireText(result.Type, 'Type');
      const target = requireText(result.Target, 'Target');
      const id = requireText(vulnerability.VulnerabilityID, 'VulnerabilityID');
      const name = requireText(vulnerability.PkgName, 'PkgName');
      // Versions, severity, module paths and branches do not change issue identity.
      const key = createHash('sha256').update(JSON.stringify([type, name, id])).digest('hex');
      if (!findings.has(key)) findings.set(key, { id, name, locations: new Set() });
      findings.get(key).locations.add(JSON.stringify({
        target,
        installedVersion: optionalText(vulnerability.InstalledVersion, 'InstalledVersion', 'Unknown'),
        fixedVersion: optionalText(vulnerability.FixedVersion, 'FixedVersion', 'No fix listed'),
        severity: optionalText(vulnerability.Severity, 'Severity', 'UNKNOWN'),
      }));
    }
  }
  return findings;
}

function issueBody(key, finding, context) {
  const { owner, repo } = context.repo;
  const run = `${context.serverUrl}/${owner}/${repo}/actions/runs/${context.runId}`;
  const locations = [...finding.locations].sort().map(JSON.parse);
  // Bound issue size while preserving every location in the report artifact.
  let shown = locations.length;
  let body;
  do {
    body = [
      `<!-- trivy:v1:${key} -->`,
      `Trivy detected **${finding.id}** in **${finding.name}**.`,
      '',
      `Scan: ${run}`,
      `Commit: ${context.sha}`,
      `Ref: ${context.ref}`,
      'The full JSON report is available in the `trivy-report` artifact on the scan run.',
      '',
      `Affected locations (${shown} of ${locations.length} shown):`,
      '```json',
      JSON.stringify(locations.slice(0, shown), null, 2),
      '```',
    ].join('\n');
    if (Buffer.byteLength(body, 'utf8') <= 60000) return body;
    shown = Math.floor(shown / 2);
  } while (shown > 0);
  throw new Error(`Issue body too large for ${finding.id}; inspect the scan artifact`);
}

module.exports = async function syncIssues({ github, context, core, report, dryRun = false, pause = delay }) {
  // Validate and prepare the whole report before making any API writes.
  const findings = collectFindings(report);
  const planned = [...findings].map(([key, finding]) => ({
    key,
    title: `[Trivy] ${finding.id} in ${finding.name}`.slice(0, 240),
    body: issueBody(key, finding, context),
  }));
  const summary = { findings: findings.size, existing: 0, created: 0, wouldCreate: 0 };
  if (planned.length === 0) {
    core.info('Trivy reported no vulnerabilities.');
    return summary;
  }

  // Paginate all states: titles, labels and issue state may change.
  // Let lookup errors fail the run; an incomplete lookup could cause duplicates.
  const existing = await github.paginate(github.rest.issues.listForRepo, {
    ...context.repo, state: 'all', per_page: 100,
  });
  const known = new Set();
  for (const issue of existing) {
    if (issue.pull_request) continue;
    for (const match of (issue.body || '').matchAll(/<!-- trivy:v1:([a-f0-9]{64}) -->/g)) {
      known.add(match[1]);
    }
  }

  for (const issue of planned) {
    if (known.has(issue.key)) {
      summary.existing++;
      continue;
    }
    if (dryRun) {
      core.info(`Would create: ${issue.title}`);
      summary.wouldCreate++;
    } else {
      // GitHub recommends at least one second between mutative requests.
      if (summary.created > 0) await pause(1000);
      // No automatic retries: a lost create response may already have created
      // the issue. A rerun will list issues again and find its marker.
      await github.rest.issues.create({ ...context.repo, title: issue.title, body: issue.body });
      summary.created++;
    }
    known.add(issue.key);
  }
  core.info(`Trivy issues: ${JSON.stringify(summary)}`);
  return summary;
};
