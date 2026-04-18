import type { LogExcerptBlock } from '../types';

export const logExcerptsByStepId: Record<string, LogExcerptBlock> = {
  'step-991-jest': {
    stepId: 'step-991-jest',
    byteCount: 784,
    githubUrl: 'https://github.com/wwa/control-tower-ui/actions/runs/991/job/211/steps/5',
    truncated: false,
    lines: [
      { lineNumber: 112, text: 'Running workspace verification with live gateway fixtures', redacted: false },
      { lineNumber: 113, text: 'AWS access key detected in fixture: ***REDACTED:AWS_KEY***', redacted: true },
      { lineNumber: 114, text: 'GitHub token masked: ***REDACTED:GH_TOKEN***', redacted: true },
      { lineNumber: 115, text: 'Authorization: Bearer ***REDACTED:BEARER***', redacted: true },
      { lineNumber: 116, text: 'Expected story linkage badge to render, but chip was hidden.', redacted: false },
      { lineNumber: 117, text: 'AssertionError: expected \"Traceability\" to be visible', redacted: false },
    ],
  },
  'step-992-e2e': {
    stepId: 'step-992-e2e',
    byteCount: 401,
    githubUrl: 'https://github.com/wwa/control-tower-ui/actions/runs/992/job/221/steps/4',
    truncated: false,
    lines: [
      { lineNumber: 81, text: 'Launching smoke journey against checkout shadow environment', redacted: false },
      { lineNumber: 82, text: 'All assertions passed in 84.21s', redacted: false },
    ],
  },
  'step-993-preview': {
    stepId: 'step-993-preview',
    byteCount: 522,
    githubUrl: 'https://github.com/wwa/ctl-observability/actions/runs/993/job/231/steps/3',
    truncated: false,
    lines: [
      { lineNumber: 54, text: 'Provisioning preview telemetry pipeline...', redacted: false },
      { lineNumber: 55, text: 'Waiting for ephemeral runner capacity...', redacted: false },
    ],
  },
  'step-994-matrix': {
    stepId: 'step-994-matrix',
    byteCount: 349,
    githubUrl: 'https://github.com/wwa/checkout-ci/actions/runs/994/job/241/steps/4',
    truncated: false,
    lines: [
      { lineNumber: 33, text: 'Matrix execution cancelled after dependency lane override.', redacted: false },
      { lineNumber: 34, text: 'Cancellation requested by release-duty-rotation', redacted: false },
    ],
  },
};

export const combinedRunLogs: Record<string, LogExcerptBlock> = {
  'run-991': {
    stepId: 'step-991-combined',
    byteCount: 1098,
    githubUrl: 'https://github.com/wwa/control-tower-ui/actions/runs/991',
    truncated: false,
    lines: [
      { lineNumber: 1, text: '== verify-pull-request ==', redacted: false },
      { lineNumber: 2, text: 'checkout complete', redacted: false },
      { lineNumber: 3, text: 'dependencies restored', redacted: false },
      { lineNumber: 4, text: 'fixture token: ***REDACTED:GH_TOKEN***', redacted: true },
      { lineNumber: 5, text: 'Authorization: Bearer ***REDACTED:BEARER***', redacted: true },
      { lineNumber: 6, text: 'Expected story-link badge not found in PR detail card', redacted: false },
      { lineNumber: 7, text: 'Test suite failed after 2 retries', redacted: false },
    ],
  },
  'run-992': {
    stepId: 'step-992-combined',
    byteCount: 512,
    githubUrl: 'https://github.com/wwa/control-tower-ui/actions/runs/992',
    truncated: false,
    lines: [
      { lineNumber: 1, text: '== main pipeline ==', redacted: false },
      { lineNumber: 2, text: 'build, lint, and e2e completed successfully', redacted: false },
    ],
  },
  'run-993': {
    stepId: 'step-993-combined',
    byteCount: 301,
    githubUrl: 'https://github.com/wwa/ctl-observability/actions/runs/993',
    truncated: false,
    lines: [
      { lineNumber: 1, text: 'Preview environment bootstrapping in progress', redacted: false },
      { lineNumber: 2, text: 'Awaiting remote worker slot allocation', redacted: false },
    ],
  },
  'run-994': {
    stepId: 'step-994-combined',
    byteCount: 211,
    githubUrl: 'https://github.com/wwa/checkout-ci/actions/runs/994',
    truncated: false,
    lines: [
      { lineNumber: 1, text: 'Rerun cancelled before matrix phase completed', redacted: false },
    ],
  },
  'run-995': {
    stepId: 'step-995-combined',
    byteCount: 175,
    githubUrl: 'https://github.com/wwa/control-tower-api/actions/runs/995',
    truncated: false,
    lines: [
      { lineNumber: 1, text: 'API release guardrail checks passed', redacted: false },
    ],
  },
  'run-996': {
    stepId: 'step-996-combined',
    byteCount: 160,
    githubUrl: 'https://github.com/wwa/checkout-web/actions/runs/996',
    truncated: false,
    lines: [
      { lineNumber: 1, text: 'Frontend canary completed with zero snapshot drift', redacted: false },
    ],
  },
};

