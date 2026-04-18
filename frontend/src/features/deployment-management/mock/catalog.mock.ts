import type { CatalogApplicationTile, CatalogSection, EnvRevisionChip } from '../types/catalog';
import type { HealthLed } from '../types/enums';

function envChip(
  name: string,
  version: string | null,
  state: import('../types/enums').DeployState | null,
  isRolledBack = false,
): EnvRevisionChip {
  return {
    environmentName: name,
    revisionReleaseVersion: version,
    deployState: state,
    deployedAt: version ? '2026-04-17T14:30:00Z' : null,
    isRolledBack,
  };
}

function tile(
  id: string,
  name: string,
  projectId: string,
  workspaceId: string,
  runtime: string,
  envs: EnvRevisionChip[],
  led: HealthLed,
  desc?: string,
): CatalogApplicationTile {
  return {
    applicationId: id,
    name,
    projectId,
    workspaceId,
    runtimeLabel: runtime,
    lastDeployAt: '2026-04-17T14:30:00Z',
    environmentRevisions: envs,
    aggregateLed: led,
    description: desc,
  };
}

const P1_APPS: CatalogApplicationTile[] = [
  tile('app-order-svc', 'Order Service', 'proj-commerce', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0042', 'SUCCEEDED'),
    envChip('qa', '2026.04.17-0041', 'SUCCEEDED'),
    envChip('staging', '2026.04.16-0039', 'SUCCEEDED'),
    envChip('prod', '2026.04.15-0038', 'SUCCEEDED'),
  ], 'GREEN', 'Core order processing microservice'),
  tile('app-payment-gw', 'Payment Gateway', 'proj-commerce', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0015', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0014', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0013', 'FAILED'),
    envChip('prod', '2026.04.14-0012', 'SUCCEEDED'),
  ], 'AMBER', 'PCI-compliant payment processing'),
  tile('app-cart-ui', 'Cart UI', 'proj-commerce', 'ws-default-001', 'node', [
    envChip('dev', '2026.04.17-0088', 'IN_PROGRESS'),
    envChip('qa', '2026.04.16-0087', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0086', 'SUCCEEDED'),
    envChip('prod', '2026.04.14-0085', 'SUCCEEDED'),
  ], 'GREEN', 'Shopping cart frontend'),
  tile('app-inventory', 'Inventory Service', 'proj-commerce', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0033', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0032', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0031', 'SUCCEEDED'),
    envChip('prod', '2026.04.14-0030', 'ROLLED_BACK', true),
  ], 'RED', 'Inventory and warehouse management'),
];

const P2_APPS: CatalogApplicationTile[] = [
  tile('app-user-auth', 'User Auth', 'proj-identity', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0021', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0020', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0019', 'SUCCEEDED'),
    envChip('prod', '2026.04.14-0018', 'SUCCEEDED'),
  ], 'GREEN', 'OAuth2 + SAML identity provider'),
  tile('app-profile-svc', 'Profile Service', 'proj-identity', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0009', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0008', 'SUCCEEDED'),
    envChip('staging', null, null),
    envChip('prod', null, null),
  ], 'UNKNOWN', 'User profile and preferences'),
  tile('app-sso-portal', 'SSO Portal', 'proj-identity', 'ws-default-001', 'node', [
    envChip('dev', '2026.04.17-0055', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0054', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0053', 'SUCCEEDED'),
    envChip('prod', '2026.04.14-0052', 'SUCCEEDED'),
  ], 'GREEN', 'Single sign-on web portal'),
];

const P3_APPS: CatalogApplicationTile[] = [
  tile('app-notif-engine', 'Notification Engine', 'proj-platform', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0066', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0065', 'FAILED'),
    envChip('staging', '2026.04.14-0063', 'SUCCEEDED'),
    envChip('prod', '2026.04.13-0062', 'SUCCEEDED'),
  ], 'AMBER', 'Multi-channel notification dispatcher'),
  tile('app-config-svc', 'Config Service', 'proj-platform', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0044', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0043', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0042', 'SUCCEEDED'),
    envChip('prod', '2026.04.14-0041', 'SUCCEEDED'),
  ], 'GREEN', 'Centralized configuration management'),
  tile('app-audit-log', 'Audit Log', 'proj-platform', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0077', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0076', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0075', 'SUCCEEDED'),
    envChip('prod', '2026.04.14-0074', 'SUCCEEDED'),
  ], 'GREEN', 'Immutable audit trail service'),
];

const P4_APPS: CatalogApplicationTile[] = [
  tile('app-search-api', 'Search API', 'proj-data', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0011', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0010', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0009', 'SUCCEEDED'),
    envChip('prod', '2026.04.14-0008', 'SUCCEEDED'),
  ], 'GREEN', 'Elasticsearch-backed search'),
  tile('app-etl-pipeline', 'ETL Pipeline', 'proj-data', 'ws-default-001', 'jvm', [
    envChip('dev', '2026.04.17-0022', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0021', 'CANCELLED'),
    envChip('staging', '2026.04.14-0019', 'SUCCEEDED'),
    envChip('prod', '2026.04.13-0018', 'SUCCEEDED'),
  ], 'AMBER', 'Data ingestion and transformation'),
  tile('app-report-gen', 'Report Generator', 'proj-data', 'ws-default-001', 'node', [
    envChip('dev', '2026.04.17-0033', 'SUCCEEDED'),
    envChip('qa', '2026.04.16-0032', 'SUCCEEDED'),
    envChip('staging', '2026.04.15-0031', 'SUCCEEDED'),
    envChip('prod', '2026.04.14-0030', 'SUCCEEDED'),
  ], 'GREEN', 'PDF/Excel report generation'),
  tile('app-ml-serving', 'ML Serving', 'proj-data', 'ws-default-001', 'python', [
    envChip('dev', '2026.04.17-0044', 'PENDING'),
    envChip('qa', null, null),
    envChip('staging', null, null),
    envChip('prod', null, null),
  ], 'UNKNOWN', 'Model inference endpoint'),
];

export const MOCK_CATALOG_SECTIONS: ReadonlyArray<CatalogSection> = [
  { projectId: 'proj-commerce', projectName: 'Commerce Platform', applications: P1_APPS },
  { projectId: 'proj-identity', projectName: 'Identity & Access', applications: P2_APPS },
  { projectId: 'proj-platform', projectName: 'Platform Services', applications: P3_APPS },
  { projectId: 'proj-data', projectName: 'Data & Analytics', applications: P4_APPS },
];
