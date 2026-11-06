const { test } = require('node:test');
const assert = require('node:assert/strict');
const syncIssues = require('./trivy-issues.cjs');

function fixture() {
  const issues = [];
  const calls = { list: [], create: [], pauses: [] };
  const input = {
    context: {
      repo: { owner: 'example', repo: 'project' }, serverUrl: 'https://github.com',
      runId: 123, sha: 'abc123', ref: 'refs/heads/test',
    },
    core: { info() {} },
    pause: async milliseconds => { calls.pauses.push(milliseconds); },
    report: {
      SchemaVersion: 2,
      Results: ['a/pom.xml', 'b/pom.xml'].map(Target => ({
        Target, Type: 'pom', Vulnerabilities: [{
          VulnerabilityID: 'CVE-TEST-1', PkgName: 'example:library',
          InstalledVersion: '1.0', FixedVersion: '2.0', Severity: 'HIGH',
        }],
      })),
    },
    github: {
      paginate: async (method, options) => {
        assert.equal(method, input.github.rest.issues.listForRepo);
        calls.list.push(options);
        return [...issues];
      },
      rest: { issues: {
        listForRepo() {},
        create: async issue => {
          calls.create.push(issue);
          const created = { ...issue, number: issues.length + 1, state: 'open' };
          issues.push(created);
          return { data: created };
        },
      } },
    },
  };
  return { input, issues, calls };
}

test('groups modules into one issue with remediation details and scan provenance', async () => {
  const { input, issues } = fixture();
  assert.deepEqual(await syncIssues(input), { findings: 1, existing: 0, created: 1, wouldCreate: 0 });
  assert.equal(issues.length, 1);
  assert.equal(issues[0].title, '[Trivy] CVE-TEST-1 in example:library');
  assert.match(issues[0].body, /a\/pom.xml/);
  assert.match(issues[0].body, /b\/pom.xml/);
  assert.match(issues[0].body, /"fixedVersion": "2.0"/);
  assert.match(issues[0].body, /https:\/\/github.com\/example\/project\/actions\/runs\/123/);
  assert.match(issues[0].body, /abc123/);
});

test('repeated reports, renamed titles and closed issues never create duplicates', async () => {
  const { input, issues, calls } = fixture();
  await syncIssues(input);
  await syncIssues(input);
  issues[0].title = 'Renamed by a maintainer';
  issues[0].state = 'closed';
  issues[0].labels = [];
  const summary = await syncIssues(input);
  assert.equal(calls.create.length, 1);
  assert.equal(summary.existing, 1);
  assert.deepEqual(calls.list[0], { owner: 'example', repo: 'project', state: 'all', per_page: 100 });
});

test('v1, v2 and v3 markers allow a fresh v4 issue, then subsequent scans skip the v4 issue', async () => {
  for (const legacyVersion of ['v1', 'v2', 'v3']) {
    const { input, issues, calls } = fixture();
    await syncIssues(input);
    assert.match(issues[0].body, /^<!-- trivy:v4:[a-f0-9]{64} -->/);
    issues[0].body = issues[0].body.replace('trivy:v4:', `trivy:${legacyVersion}:`);
    const legacyBody = issues[0].body;

    assert.deepEqual(await syncIssues(input), { findings: 1, existing: 0, created: 1, wouldCreate: 0 });
    assert.equal(issues[0].body, legacyBody);
    assert.match(issues[1].body, /^<!-- trivy:v4:[a-f0-9]{64} -->/);

    assert.deepEqual(await syncIssues(input), { findings: 1, existing: 1, created: 0, wouldCreate: 0 });
    assert.equal(calls.create.length, 2);
  }
});

test('version, severity, module and branch changes preserve identity', async () => {
  const { input, calls } = fixture();
  await syncIssues(input);
  input.context.ref = 'refs/heads/other';
  input.report.Results.reverse();
  input.report.Results[0].Target = 'renamed/pom.xml';
  Object.assign(input.report.Results[0].Vulnerabilities[0], {
    InstalledVersion: '1.1', FixedVersion: '2.1', Severity: 'CRITICAL',
  });
  await syncIssues(input);
  assert.equal(calls.create.length, 1);
});

test('different advisory IDs and packages create separate issues', async () => {
  const { input, calls } = fixture();
  await syncIssues(input);
  input.report.Results[0].Vulnerabilities[0].VulnerabilityID = 'CVE-TEST-2';
  await syncIssues(input);
  input.report.Results[0].Vulnerabilities[0].PkgName = 'example:other';
  await syncIssues(input);
  assert.equal(calls.create.length, 3);
});

test('Maven source, SBOM and archive findings share the existing issue identity', async () => {
  const { input, issues, calls } = fixture();
  await syncIssues(input);
  input.report.Results.push({
    ...input.report.Results[0], Type: 'jar', Target: 'dependency-sbom.json',
  });
  assert.deepEqual(await syncIssues(input), { findings: 1, existing: 1, created: 0, wouldCreate: 0 });
  assert.equal(calls.create.length, 1);

  issues.length = 0;
  await syncIssues(input);
  assert.match(issues[0].body, /a\/pom.xml/);
  assert.match(issues[0].body, /dependency-sbom.json/);
});

test('matches returned beyond the first page are recognized', async () => {
  const { input, issues, calls } = fixture();
  await syncIssues(input);
  issues.unshift(...Array.from({ length: 150 }, () => ({ body: 'Unrelated issue', state: 'closed' })));
  await syncIssues(input);
  assert.equal(calls.create.length, 1);
  assert.equal(calls.list[1].per_page, 100);
});

test('a matching pull request is not treated as an existing issue', async () => {
  const { input, issues, calls } = fixture();
  await syncIssues(input);
  issues[0].pull_request = { url: 'https://api.github.com/repos/example/project/pulls/1' };
  await syncIssues(input);
  assert.equal(calls.create.length, 2);
});

test('dry runs report new findings without creating or updating issues', async () => {
  const { input, issues, calls } = fixture();
  const preview = await syncIssues({ ...input, dryRun: true });
  assert.deepEqual(preview, { findings: 1, existing: 0, created: 0, wouldCreate: 1 });
  assert.equal(issues.length, 0);
  assert.equal(calls.create.length, 0);
  await syncIssues(input);
  assert.deepEqual(await syncIssues({ ...input, dryRun: true }), {
    findings: 1, existing: 1, created: 0, wouldCreate: 0,
  });
});

test('API lookup failure prevents any creation', async () => {
  const { input, calls } = fixture();
  input.github.paginate = async () => { throw new Error('API lookup failed'); };
  await assert.rejects(syncIssues(input), /API lookup failed/);
  assert.equal(calls.create.length, 0);
});

test('multiple new issues are created serially with a pause between writes', async () => {
  const { input, calls } = fixture();
  input.report.Results[1].Vulnerabilities[0].VulnerabilityID = 'CVE-TEST-2';
  const create = input.github.rest.issues.create;
  input.github.rest.issues.create = async issue => {
    assert.equal(calls.pauses.length, calls.create.length);
    return create(issue);
  };
  await syncIssues(input);
  assert.equal(calls.create.length, 2);
  assert.deepEqual(calls.pauses, [1000]);
});

test('a lost create response can be recovered by rerunning without duplicates', async () => {
  const { input, issues, calls } = fixture();
  const create = input.github.rest.issues.create;
  input.github.rest.issues.create = async issue => {
    await create(issue);
    throw new Error('Connection lost after creation');
  };
  await assert.rejects(syncIssues(input), /Connection lost/);
  input.github.rest.issues.create = create;
  await syncIssues(input);
  assert.equal(issues.length, 1);
  assert.equal(calls.create.length, 1);
});

test('malformed reports fail before API access, even after valid findings', async () => {
  for (const report of [null, {}, { SchemaVersion: 1 }, { SchemaVersion: 2, Results: {} },
    { SchemaVersion: 2, Results: [null] },
    { SchemaVersion: 2, Results: [{ Vulnerabilities: {} }] }]) {
    const { input, calls } = fixture();
    await assert.rejects(syncIssues({ ...input, report }), /malformed/i);
    assert.equal(calls.list.length, 0);
    assert.equal(calls.create.length, 0);
  }
  const { input, calls } = fixture();
  input.report.Results[1].Vulnerabilities[0].PkgName = '';
  await assert.rejects(syncIssues(input), /PkgName/);
  assert.equal(calls.list.length, 0);
  assert.equal(calls.create.length, 0);
});

test('empty reports create nothing and do not access the API', async () => {
  for (const Results of [undefined, [], [{ Target: 'pom.xml', Type: 'pom' }]]) {
    const { input, calls } = fixture();
    const summary = await syncIssues({ ...input, report: { SchemaVersion: 2, Results } });
    assert.deepEqual(summary, { findings: 0, existing: 0, created: 0, wouldCreate: 0 });
    assert.equal(calls.list.length, 0);
    assert.equal(calls.create.length, 0);
  }
});

test('unfixed vulnerabilities remain reportable', async () => {
  const { input, issues } = fixture();
  delete input.report.Results[0].Vulnerabilities[0].FixedVersion;
  await syncIssues(input);
  assert.match(issues[0].body, /No fix listed/);
});

test('large findings truncate locations instead of exceeding the issue body limit', async () => {
  const { input, issues } = fixture();
  const template = input.report.Results[0];
  input.report.Results = Array.from({ length: 1000 }, (_, i) => ({
    ...template, Target: `module-${i}/pom.xml`,
  }));
  await syncIssues(input);
  assert.equal(issues.length, 1);
  assert.ok(Buffer.byteLength(issues[0].body, 'utf8') <= 60000);
  assert.match(issues[0].body, /of 1000 shown/);
  assert.match(issues[0].body, /trivy-report/);
});
