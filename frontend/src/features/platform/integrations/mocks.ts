import type { Connection, AdapterDescriptor, ConnectionTestResult } from '../shared/types';

export const MOCK_ADAPTERS: AdapterDescriptor[] = [
  { kind: 'jira', label: 'Jira', supportedModes: ['pull', 'push', 'both'], capabilities: ['requirement', 'issue-status'] },
  { kind: 'confluence', label: 'Confluence', supportedModes: ['pull'], capabilities: ['requirement-source', 'business-context'] },
  { kind: 'gitlab', label: 'GitLab', supportedModes: ['pull', 'both'], capabilities: ['code-change', 'merge-request', 'branch'] },
  { kind: 'jenkins', label: 'Jenkins', supportedModes: ['push', 'both'], capabilities: ['build', 'pipeline', 'quality-gate'] },
  { kind: 'servicenow', label: 'ServiceNow', supportedModes: ['pull', 'push', 'both'], capabilities: ['incident', 'change', 'oncall'] },
  { kind: 'custom-webhook', label: 'Custom Webhook', supportedModes: ['push'], capabilities: ['generic'] },
];

export const MOCK_CONNECTIONS: Connection[] = [
  { id: 'conn-001', kind: 'jira', scopeWorkspaceId: 'ws-default', applicationId: 'app-payment-gateway-pro', applicationName: 'Payment-Gateway-Pro', snowGroupId: 'snow-fin-tech-ops', snowGroupName: 'FIN-TECH-OPS', baseUrl: 'https://jira.company.com', credentialRef: 'cred-jira-demo', syncMode: 'both', pullSchedule: '0 */15 * * * *', pushUrl: 'https://webhook.local/jira', status: 'enabled', lastSyncAt: '2026-04-18T09:00:00Z', lastTestAt: '2026-04-18T08:00:00Z', lastTestOk: true },
  { id: 'conn-007', kind: 'confluence', scopeWorkspaceId: 'ws-default', applicationId: 'app-payment-gateway-pro', applicationName: 'Payment-Gateway-Pro', snowGroupId: 'snow-fin-tech-ops', snowGroupName: 'FIN-TECH-OPS', baseUrl: 'https://confluence.company.com', credentialRef: 'cred-confluence-demo', syncMode: 'pull', pullSchedule: '0 */15 * * * *', pushUrl: null, status: 'enabled', lastSyncAt: '2026-04-18T09:02:00Z', lastTestAt: '2026-04-18T08:02:00Z', lastTestOk: true },
  { id: 'conn-002', kind: 'gitlab', scopeWorkspaceId: 'ws-default', applicationId: 'app-payment-gateway-pro', applicationName: 'Payment-Gateway-Pro', snowGroupId: 'snow-fin-tech-ops', snowGroupName: 'FIN-TECH-OPS', baseUrl: 'https://gitlab.company.com', credentialRef: 'cred-gitlab-demo', syncMode: 'both', pullSchedule: '0 */10 * * * *', pushUrl: null, status: 'enabled', lastSyncAt: '2026-04-18T09:05:00Z', lastTestAt: '2026-04-18T08:05:00Z', lastTestOk: true },
  { id: 'conn-003', kind: 'jenkins', scopeWorkspaceId: 'ws-default', applicationId: 'app-payment-gateway-pro', applicationName: 'Payment-Gateway-Pro', snowGroupId: 'snow-fin-tech-ops', snowGroupName: 'FIN-TECH-OPS', baseUrl: 'https://jenkins.company.com', credentialRef: 'cred-jenkins-demo', syncMode: 'push', pullSchedule: null, pushUrl: 'https://webhook.local/jenkins', status: 'enabled', lastSyncAt: '2026-04-18T08:30:00Z', lastTestAt: '2026-04-17T20:00:00Z', lastTestOk: true },
  { id: 'conn-004', kind: 'servicenow', scopeWorkspaceId: 'ws-default', applicationId: null, applicationName: null, snowGroupId: 'snow-fin-tech-ops', snowGroupName: 'FIN-TECH-OPS', baseUrl: 'https://company.service-now.com', credentialRef: 'cred-snow-demo', syncMode: 'pull', pullSchedule: '0 */30 * * * *', pushUrl: null, status: 'disabled', lastSyncAt: null, lastTestAt: null, lastTestOk: null },
  { id: 'conn-005', kind: 'custom-webhook', scopeWorkspaceId: 'ws-default', applicationId: null, applicationName: null, snowGroupId: 'snow-platform-ops', snowGroupName: 'PLATFORM-OPS', baseUrl: null, credentialRef: 'cred-webhook-demo', syncMode: 'push', pullSchedule: null, pushUrl: 'https://webhook.local/custom', status: 'error', lastSyncAt: '2026-04-17T12:00:00Z', lastTestAt: '2026-04-18T06:00:00Z', lastTestOk: false },
  { id: 'conn-006', kind: 'jira', scopeWorkspaceId: 'ws-fin', applicationId: 'app-finance-core', applicationName: 'Finance-Core', snowGroupId: 'snow-fin-apps', snowGroupName: 'FIN-APPS', baseUrl: 'https://jira.finance.company.com', credentialRef: 'cred-jira-fin', syncMode: 'pull', pullSchedule: '0 */20 * * * *', pushUrl: null, status: 'enabled', lastSyncAt: '2026-04-18T09:10:00Z', lastTestAt: '2026-04-18T07:00:00Z', lastTestOk: true },
];

export const MOCK_TEST_RESULT_OK: ConnectionTestResult = { ok: true, latencyMs: 182, message: 'Authenticated as demo-bot' };
export const MOCK_TEST_RESULT_FAIL: ConnectionTestResult = { ok: false, latencyMs: 10000, message: 'Timeout after 10s' };
