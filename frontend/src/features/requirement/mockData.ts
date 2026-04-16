import type { RequirementList, RequirementDetail } from './types/requirement';

/**
 * Mock requirement list — matches API guide §2.2
 */
export const MOCK_REQUIREMENT_LIST: RequirementList = {
  statusDistribution: {
    draft: 2,
    inReview: 2,
    approved: 2,
    inProgress: 2,
    delivered: 1,
    archived: 1,
  },
  requirements: [
    {
      id: 'REQ-0001',
      title: 'User Authentication and SSO Integration',
      priority: 'Critical',
      status: 'Approved',
      category: 'Functional',
      storyCount: 4,
      specCount: 2,
      completeness: 85,
      updatedAt: '2026-04-16T10:30:00Z',
    },
    {
      id: 'REQ-0002',
      title: 'Role-Based Access Control',
      priority: 'High',
      status: 'In Progress',
      category: 'Functional',
      storyCount: 3,
      specCount: 1,
      completeness: 60,
      updatedAt: '2026-04-16T09:15:00Z',
    },
    {
      id: 'REQ-0003',
      title: 'API Response Time Under 200ms',
      priority: 'High',
      status: 'Draft',
      category: 'Non-Functional',
      storyCount: 0,
      specCount: 0,
      completeness: 15,
      updatedAt: '2026-04-15T16:00:00Z',
    },
    {
      id: 'REQ-0004',
      title: 'Database Migration to Oracle 23ai',
      priority: 'Medium',
      status: 'In Review',
      category: 'Technical',
      storyCount: 2,
      specCount: 1,
      completeness: 45,
      updatedAt: '2026-04-15T14:20:00Z',
    },
    {
      id: 'REQ-0005',
      title: 'Audit Trail for Compliance',
      priority: 'Critical',
      status: 'In Progress',
      category: 'Business',
      storyCount: 3,
      specCount: 2,
      completeness: 70,
      updatedAt: '2026-04-16T08:45:00Z',
    },
    {
      id: 'REQ-0006',
      title: 'User Profile Management',
      priority: 'Low',
      status: 'Delivered',
      category: 'Functional',
      storyCount: 2,
      specCount: 1,
      completeness: 100,
      updatedAt: '2026-04-14T11:00:00Z',
    },
    {
      id: 'REQ-0007',
      title: '99.9% Uptime SLA',
      priority: 'Medium',
      status: 'Approved',
      category: 'Non-Functional',
      storyCount: 1,
      specCount: 1,
      completeness: 50,
      updatedAt: '2026-04-14T09:30:00Z',
    },
    {
      id: 'REQ-0008',
      title: 'Event-Driven Architecture Migration',
      priority: 'High',
      status: 'Draft',
      category: 'Technical',
      storyCount: 0,
      specCount: 0,
      completeness: 10,
      updatedAt: '2026-04-13T17:00:00Z',
    },
    {
      id: 'REQ-0009',
      title: 'Legacy Report Export',
      priority: 'Medium',
      status: 'Archived',
      category: 'Business',
      storyCount: 2,
      specCount: 1,
      completeness: 100,
      updatedAt: '2026-04-10T10:00:00Z',
    },
    {
      id: 'REQ-0010',
      title: 'AI-Powered Requirement Analysis',
      priority: 'High',
      status: 'In Review',
      category: 'Functional',
      storyCount: 2,
      specCount: 0,
      completeness: 35,
      updatedAt: '2026-04-16T07:00:00Z',
    },
  ],
};

/**
 * Mock requirement detail for REQ-0001 — matches API guide §3.2
 */
export const MOCK_REQUIREMENT_DETAIL_0001: RequirementDetail = {
  header: {
    data: {
      id: 'REQ-0001',
      title: 'User Authentication and SSO Integration',
      priority: 'Critical',
      status: 'Approved',
      category: 'Functional',
      source: 'Manual',
      assignee: 'Sarah Chen',
      createdAt: '2026-04-10T09:00:00Z',
      updatedAt: '2026-04-16T10:30:00Z',
    },
    error: null,
  },
  description: {
    data: {
      summary: 'Implement enterprise SSO authentication using SAML 2.0 and OAuth 2.0 protocols to enable single sign-on across all SDLC Control Tower modules. Must support Active Directory, Okta, and Azure AD identity providers.',
      businessJustification: 'Enterprise customers require SSO integration for security compliance (SOC 2, ISO 27001). Without SSO, adoption is blocked for 60% of target enterprise accounts. Current username/password auth does not meet enterprise security policies.',
      acceptanceCriteria: [
        { id: 'AC-001', text: 'Users can authenticate via SAML 2.0 SSO with their corporate IdP', isMet: true },
        { id: 'AC-002', text: 'OAuth 2.0 authorization code flow works with Okta and Azure AD', isMet: true },
        { id: 'AC-003', text: 'Session tokens expire after 8 hours of inactivity', isMet: false },
        { id: 'AC-004', text: 'Failed login attempts are rate-limited to 5 per minute per IP', isMet: true },
        { id: 'AC-005', text: 'Admin can configure IdP settings through Platform Center', isMet: false },
      ],
      assumptions: [
        'Customer IdP supports SAML 2.0 or OAuth 2.0',
        'Network connectivity to external IdP endpoints is available',
        'Certificate management for SAML signing is handled externally',
      ],
      constraints: [
        'Must not introduce latency > 500ms to login flow',
        'Must work behind corporate proxies',
        'Must support multi-tenant isolation',
      ],
    },
    error: null,
  },
  linkedStories: {
    data: {
      stories: [
        { id: 'US-001', title: 'As an enterprise user, I can log in via SSO', status: 'Done', specId: 'SPEC-001', specStatus: 'Approved' },
        { id: 'US-002', title: 'As an admin, I can configure SAML IdP settings', status: 'In Progress', specId: 'SPEC-002', specStatus: 'Draft' },
        { id: 'US-003', title: 'As a user, I am automatically logged out after inactivity', status: 'Draft', specId: null, specStatus: null },
        { id: 'US-004', title: 'As a security admin, I can view failed login attempts', status: 'Ready', specId: null, specStatus: null },
      ],
      totalCount: 4,
    },
    error: null,
  },
  linkedSpecs: {
    data: {
      specs: [
        { id: 'SPEC-001', title: 'SSO Authentication Flow Specification', status: 'Approved', version: 'v1.2' },
        { id: 'SPEC-002', title: 'IdP Configuration Management Spec', status: 'Draft', version: 'v0.1' },
      ],
      totalCount: 2,
    },
    error: null,
  },
  aiAnalysis: {
    data: {
      completenessScore: 85,
      missingElements: [
        'Session timeout behavior not fully specified',
        'No error recovery flow for IdP outage',
      ],
      similarRequirements: [
        { id: 'REQ-0002', similarity: 72 },
        { id: 'REQ-0005', similarity: 45 },
      ],
      impactAssessment: 'High impact — blocks enterprise customer onboarding. Affects 8 downstream user stories across 3 modules. Critical path for Q2 release.',
      suggestions: [
        'Add specification for IdP failover behavior',
        'Define session token refresh strategy',
        'Consider adding MFA as optional enhancement',
        'Align with REQ-0002 (RBAC) for permission model',
      ],
    },
    error: null,
  },
  sdlcChain: {
    data: {
      links: [
        { artifactType: 'requirement', artifactId: 'REQ-0001', artifactTitle: 'User Authentication and SSO Integration', routePath: '/requirements/REQ-0001' },
        { artifactType: 'user-story', artifactId: 'US-001', artifactTitle: 'Enterprise SSO Login', routePath: '/requirements' },
        { artifactType: 'spec', artifactId: 'SPEC-001', artifactTitle: 'SSO Authentication Flow Specification', routePath: '/requirements' },
        { artifactType: 'design', artifactId: 'DES-001', artifactTitle: 'Auth Module Design', routePath: '/design' },
        { artifactType: 'code', artifactId: 'MR-1050', artifactTitle: 'feat: SAML 2.0 SSO integration', routePath: '/code' },
        { artifactType: 'test', artifactId: 'TS-042', artifactTitle: 'SSO Integration Test Suite', routePath: '/testing' },
      ],
    },
    error: null,
  },
};

/**
 * Mock requirement detail for REQ-0002
 */
export const MOCK_REQUIREMENT_DETAIL_0002: RequirementDetail = {
  header: {
    data: {
      id: 'REQ-0002',
      title: 'Role-Based Access Control',
      priority: 'High',
      status: 'In Progress',
      category: 'Functional',
      source: 'Manual',
      assignee: 'Alex Kim',
      createdAt: '2026-04-11T10:00:00Z',
      updatedAt: '2026-04-16T09:15:00Z',
    },
    error: null,
  },
  description: {
    data: {
      summary: 'Implement role-based access control (RBAC) with predefined roles (Admin, Manager, Developer, Viewer) and custom role support. Permissions must be enforceable at both API and UI levels.',
      businessJustification: 'Enterprise customers require granular access control for compliance. Current system has no permission model, making it unsuitable for teams larger than 10 users.',
      acceptanceCriteria: [
        { id: 'AC-010', text: 'Four predefined roles are available: Admin, Manager, Developer, Viewer', isMet: true },
        { id: 'AC-011', text: 'Custom roles can be created with specific permission sets', isMet: false },
        { id: 'AC-012', text: 'API endpoints enforce role-based permissions', isMet: true },
        { id: 'AC-013', text: 'UI elements are hidden/disabled based on user role', isMet: false },
      ],
      assumptions: [
        'Authentication (REQ-0001) is implemented before RBAC',
        'Permission checks have < 10ms overhead per request',
      ],
      constraints: [
        'Must integrate with existing SSO session tokens',
        'Permission cache must be invalidated within 30 seconds of role change',
      ],
    },
    error: null,
  },
  linkedStories: {
    data: {
      stories: [
        { id: 'US-010', title: 'As an admin, I can assign roles to team members', status: 'In Progress', specId: 'SPEC-010', specStatus: 'Approved' },
        { id: 'US-011', title: 'As a developer, I see only permitted actions in the UI', status: 'Draft', specId: null, specStatus: null },
        { id: 'US-012', title: 'As an admin, I can create custom roles', status: 'Draft', specId: null, specStatus: null },
      ],
      totalCount: 3,
    },
    error: null,
  },
  linkedSpecs: {
    data: {
      specs: [
        { id: 'SPEC-010', title: 'RBAC Permission Model Specification', status: 'Approved', version: 'v1.0' },
      ],
      totalCount: 1,
    },
    error: null,
  },
  aiAnalysis: {
    data: {
      completenessScore: 60,
      missingElements: [
        'Custom role creation workflow not detailed',
        'Permission inheritance model not specified',
        'Audit logging for role changes not addressed',
      ],
      similarRequirements: [
        { id: 'REQ-0001', similarity: 72 },
        { id: 'REQ-0005', similarity: 55 },
      ],
      impactAssessment: 'Medium-high impact — required for multi-tenant operation. Depends on REQ-0001 (SSO). Blocks REQ-0005 (Audit Trail).',
      suggestions: [
        'Define permission inheritance hierarchy',
        'Add audit logging for all role/permission changes',
        'Consider attribute-based access control (ABAC) for future extensibility',
      ],
    },
    error: null,
  },
  sdlcChain: {
    data: {
      links: [
        { artifactType: 'requirement', artifactId: 'REQ-0002', artifactTitle: 'Role-Based Access Control', routePath: '/requirements/REQ-0002' },
        { artifactType: 'user-story', artifactId: 'US-010', artifactTitle: 'Role Assignment', routePath: '/requirements' },
        { artifactType: 'spec', artifactId: 'SPEC-010', artifactTitle: 'RBAC Permission Model', routePath: '/requirements' },
      ],
    },
    error: null,
  },
};

/**
 * Mock requirement detail for REQ-0003 (Draft, no stories/specs)
 */
export const MOCK_REQUIREMENT_DETAIL_0003: RequirementDetail = {
  header: {
    data: {
      id: 'REQ-0003',
      title: 'API Response Time Under 200ms',
      priority: 'High',
      status: 'Draft',
      category: 'Non-Functional',
      source: 'AI-Generated',
      assignee: 'Unassigned',
      createdAt: '2026-04-15T16:00:00Z',
      updatedAt: '2026-04-15T16:00:00Z',
    },
    error: null,
  },
  description: {
    data: {
      summary: 'All REST API endpoints must respond within 200ms at p95 latency under normal load (100 concurrent users). Batch operations may take up to 2 seconds.',
      businessJustification: 'User experience research shows that response times over 200ms are perceived as slow. Competitor benchmarks show sub-150ms p95 latency. Enterprise SLA requires documented performance targets.',
      acceptanceCriteria: [
        { id: 'AC-020', text: 'p95 latency for single-resource GET < 200ms', isMet: false },
        { id: 'AC-021', text: 'p95 latency for list endpoints < 500ms with pagination', isMet: false },
        { id: 'AC-022', text: 'Performance regression tests run on every PR', isMet: false },
      ],
      assumptions: [
        'Database queries are optimized with proper indexing',
        'Response caching is available for read-heavy endpoints',
      ],
      constraints: [
        'Measurement must include serialization and middleware overhead',
        'Must account for multi-tenant query isolation overhead',
      ],
    },
    error: null,
  },
  linkedStories: {
    data: { stories: [], totalCount: 0 },
    error: null,
  },
  linkedSpecs: {
    data: { specs: [], totalCount: 0 },
    error: null,
  },
  aiAnalysis: {
    data: null,
    error: null,
  },
  sdlcChain: {
    data: {
      links: [
        { artifactType: 'requirement', artifactId: 'REQ-0003', artifactTitle: 'API Response Time Under 200ms', routePath: '/requirements/REQ-0003' },
      ],
    },
    error: null,
  },
};

/**
 * Mock detail for REQ-0005 (In Progress, Business)
 */
export const MOCK_REQUIREMENT_DETAIL_0005: RequirementDetail = {
  header: {
    data: {
      id: 'REQ-0005',
      title: 'Audit Trail for Compliance',
      priority: 'Critical',
      status: 'In Progress',
      category: 'Business',
      source: 'Imported',
      assignee: 'Mike Ross',
      createdAt: '2026-04-12T08:00:00Z',
      updatedAt: '2026-04-16T08:45:00Z',
    },
    error: null,
  },
  description: {
    data: {
      summary: 'Maintain a comprehensive audit trail of all user actions, system changes, and AI decisions for regulatory compliance (SOC 2 Type II, GDPR Article 30). All events must be immutable and retained for 7 years.',
      businessJustification: 'Regulatory requirement for SOC 2 certification. Legal team mandates full traceability of all system actions. Insurance underwriter requires audit capability.',
      acceptanceCriteria: [
        { id: 'AC-040', text: 'All CRUD operations are logged with actor, timestamp, and delta', isMet: true },
        { id: 'AC-041', text: 'AI decisions include reasoning chain in audit log', isMet: true },
        { id: 'AC-042', text: 'Audit logs are immutable (append-only)', isMet: false },
        { id: 'AC-043', text: 'Audit search supports date range and actor filtering', isMet: true },
        { id: 'AC-044', text: 'Retention policy enforces 7-year minimum', isMet: false },
      ],
      assumptions: [
        'Dedicated audit storage (separate from operational DB)',
        'Audit write latency does not block user operations (async)',
      ],
      constraints: [
        'Audit data must be encrypted at rest',
        'Must support export to SIEM tools (Splunk, ELK)',
        'GDPR right-to-erasure must be handled via pseudonymization, not deletion',
      ],
    },
    error: null,
  },
  linkedStories: {
    data: {
      stories: [
        { id: 'US-040', title: 'As an auditor, I can search audit events by date and actor', status: 'Done', specId: 'SPEC-040', specStatus: 'Approved' },
        { id: 'US-041', title: 'As a compliance officer, I can export audit logs', status: 'In Progress', specId: 'SPEC-041', specStatus: 'Draft' },
        { id: 'US-042', title: 'As the system, AI decisions are logged with reasoning', status: 'Ready', specId: null, specStatus: null },
      ],
      totalCount: 3,
    },
    error: null,
  },
  linkedSpecs: {
    data: {
      specs: [
        { id: 'SPEC-040', title: 'Audit Event Schema Specification', status: 'Approved', version: 'v2.0' },
        { id: 'SPEC-041', title: 'Audit Export API Specification', status: 'Draft', version: 'v0.3' },
      ],
      totalCount: 2,
    },
    error: null,
  },
  aiAnalysis: {
    data: {
      completenessScore: 70,
      missingElements: [
        'Retention policy implementation details',
        'GDPR pseudonymization strategy not specified',
      ],
      similarRequirements: [
        { id: 'REQ-0002', similarity: 55 },
      ],
      impactAssessment: 'Critical — SOC 2 certification deadline is Q3 2026. Blocks production deployment for enterprise tier.',
      suggestions: [
        'Define pseudonymization strategy for GDPR compliance',
        'Specify audit storage partitioning strategy for 7-year retention',
        'Add specification for SIEM export format (CEF or LEEF)',
      ],
    },
    error: null,
  },
  sdlcChain: {
    data: {
      links: [
        { artifactType: 'requirement', artifactId: 'REQ-0005', artifactTitle: 'Audit Trail for Compliance', routePath: '/requirements/REQ-0005' },
        { artifactType: 'user-story', artifactId: 'US-040', artifactTitle: 'Audit Event Search', routePath: '/requirements' },
        { artifactType: 'spec', artifactId: 'SPEC-040', artifactTitle: 'Audit Event Schema', routePath: '/requirements' },
        { artifactType: 'code', artifactId: 'MR-1180', artifactTitle: 'feat: audit event logging framework', routePath: '/code' },
      ],
    },
    error: null,
  },
};

/**
 * Mock requirement detail for REQ-0004
 */
export const MOCK_REQUIREMENT_DETAIL_0004: RequirementDetail = {
  header: { data: { id: 'REQ-0004', title: 'Database Migration to Oracle 23ai', priority: 'Medium', status: 'In Review', category: 'Technical', source: 'Manual', assignee: 'David Park', createdAt: '2026-04-12T09:00:00Z', updatedAt: '2026-04-15T14:20:00Z' }, error: null },
  description: { data: { summary: 'Migrate primary data store from PostgreSQL 15 to Oracle 23ai to leverage vector search, JSON Relational Duality, and enterprise-grade partitioning for the Control Tower platform.', businessJustification: 'Enterprise customers on Oracle infrastructure require native Oracle support. Oracle 23ai vector search eliminates the need for a separate vector DB, reducing operational complexity.', acceptanceCriteria: [ { id: 'AC-030', text: 'All Flyway migrations execute successfully against Oracle 23ai', isMet: true }, { id: 'AC-031', text: 'Query performance parity with PostgreSQL baseline (±10%)', isMet: false }, { id: 'AC-032', text: 'Zero data loss during migration', isMet: false } ], assumptions: ['Oracle 23ai license is procured', 'DBA team available for schema review'], constraints: ['Must maintain PostgreSQL compatibility for H2 local dev', 'Migration window is 4 hours maximum'] }, error: null },
  linkedStories: { data: { stories: [ { id: 'US-030', title: 'As a DBA, I can run Flyway migrations against Oracle 23ai', status: 'In Progress', specId: 'SPEC-030', specStatus: 'Review' }, { id: 'US-031', title: 'As a developer, I can use H2 locally while prod uses Oracle', status: 'Draft', specId: null, specStatus: null } ], totalCount: 2 }, error: null },
  linkedSpecs: { data: { specs: [ { id: 'SPEC-030', title: 'Oracle 23ai Migration Specification', status: 'Review', version: 'v0.8' } ], totalCount: 1 }, error: null },
  aiAnalysis: { data: { completenessScore: 45, missingElements: ['Rollback strategy not defined', 'Performance benchmark criteria incomplete'], similarRequirements: [{ id: 'REQ-0007', similarity: 35 }], impactAssessment: 'Medium — affects all data access layers. Must coordinate with REQ-0007 (SLA) for performance targets.', suggestions: ['Define rollback procedure for failed migration', 'Add performance benchmark suite requirement', 'Consider blue-green migration approach'] }, error: null },
  sdlcChain: { data: { links: [ { artifactType: 'requirement', artifactId: 'REQ-0004', artifactTitle: 'Database Migration to Oracle 23ai', routePath: '/requirements/REQ-0004' }, { artifactType: 'spec', artifactId: 'SPEC-030', artifactTitle: 'Oracle 23ai Migration Specification', routePath: '/requirements' } ] }, error: null },
};

/**
 * Mock requirement detail for REQ-0006
 */
export const MOCK_REQUIREMENT_DETAIL_0006: RequirementDetail = {
  header: { data: { id: 'REQ-0006', title: 'User Profile Management', priority: 'Low', status: 'Delivered', category: 'Functional', source: 'Manual', assignee: 'Emily Wang', createdAt: '2026-04-05T09:00:00Z', updatedAt: '2026-04-14T11:00:00Z' }, error: null },
  description: { data: { summary: 'Users can view and edit their profile information including display name, avatar, timezone, and notification preferences.', businessJustification: 'Basic user experience requirement. Supports personalization and timezone-aware scheduling across modules.', acceptanceCriteria: [ { id: 'AC-050', text: 'Users can update their display name and avatar', isMet: true }, { id: 'AC-051', text: 'Timezone selection affects all date/time displays', isMet: true } ], assumptions: ['Avatar storage uses existing file service'], constraints: ['Profile changes must propagate within 5 seconds'] }, error: null },
  linkedStories: { data: { stories: [ { id: 'US-050', title: 'As a user, I can update my profile settings', status: 'Done', specId: 'SPEC-050', specStatus: 'Implemented' }, { id: 'US-051', title: 'As a user, I can set my timezone preference', status: 'Done', specId: null, specStatus: null } ], totalCount: 2 }, error: null },
  linkedSpecs: { data: { specs: [ { id: 'SPEC-050', title: 'User Profile API Specification', status: 'Implemented', version: 'v1.0' } ], totalCount: 1 }, error: null },
  aiAnalysis: { data: { completenessScore: 100, missingElements: [], similarRequirements: [], impactAssessment: 'Low — self-contained feature. Fully delivered.', suggestions: [] }, error: null },
  sdlcChain: { data: { links: [ { artifactType: 'requirement', artifactId: 'REQ-0006', artifactTitle: 'User Profile Management', routePath: '/requirements/REQ-0006' }, { artifactType: 'code', artifactId: 'MR-1095', artifactTitle: 'feat: user profile management', routePath: '/code' }, { artifactType: 'test', artifactId: 'TS-020', artifactTitle: 'Profile Management Tests', routePath: '/testing' } ] }, error: null },
};

/**
 * Mock requirement detail for REQ-0007
 */
export const MOCK_REQUIREMENT_DETAIL_0007: RequirementDetail = {
  header: { data: { id: 'REQ-0007', title: '99.9% Uptime SLA', priority: 'Medium', status: 'Approved', category: 'Non-Functional', source: 'Imported', assignee: 'Sarah Chen', createdAt: '2026-04-08T10:00:00Z', updatedAt: '2026-04-14T09:30:00Z' }, error: null },
  description: { data: { summary: 'The SDLC Control Tower platform must maintain 99.9% uptime measured monthly, excluding planned maintenance windows. This translates to a maximum of 43.8 minutes of unplanned downtime per month.', businessJustification: 'Enterprise SLA commitment. Contractual obligation for Tier 1 customers. Failure to meet SLA triggers service credits.', acceptanceCriteria: [ { id: 'AC-060', text: 'Health check endpoints respond within 2 seconds', isMet: true }, { id: 'AC-061', text: 'Automated failover completes within 60 seconds', isMet: false }, { id: 'AC-062', text: 'Monthly uptime report is auto-generated', isMet: false } ], assumptions: ['Multi-AZ deployment is available', 'Database replication is configured'], constraints: ['Planned maintenance windows must not exceed 4 hours/month', 'Failover must be transparent to active users'] }, error: null },
  linkedStories: { data: { stories: [ { id: 'US-060', title: 'As an SRE, I can view real-time uptime metrics', status: 'Ready', specId: 'SPEC-060', specStatus: 'Approved' } ], totalCount: 1 }, error: null },
  linkedSpecs: { data: { specs: [ { id: 'SPEC-060', title: 'SLA Monitoring and Reporting Spec', status: 'Approved', version: 'v1.0' } ], totalCount: 1 }, error: null },
  aiAnalysis: { data: { completenessScore: 50, missingElements: ['Failover procedure not documented', 'Alerting thresholds not specified'], similarRequirements: [{ id: 'REQ-0004', similarity: 35 }], impactAssessment: 'Medium — operational requirement that affects deployment architecture and monitoring infrastructure.', suggestions: ['Define alerting escalation chain', 'Specify RTO and RPO targets', 'Add chaos testing requirement'] }, error: null },
  sdlcChain: { data: { links: [ { artifactType: 'requirement', artifactId: 'REQ-0007', artifactTitle: '99.9% Uptime SLA', routePath: '/requirements/REQ-0007' }, { artifactType: 'spec', artifactId: 'SPEC-060', artifactTitle: 'SLA Monitoring Spec', routePath: '/requirements' } ] }, error: null },
};

/**
 * Mock requirement detail for REQ-0008
 */
export const MOCK_REQUIREMENT_DETAIL_0008: RequirementDetail = {
  header: { data: { id: 'REQ-0008', title: 'Event-Driven Architecture Migration', priority: 'High', status: 'Draft', category: 'Technical', source: 'AI-Generated', assignee: 'Unassigned', createdAt: '2026-04-13T17:00:00Z', updatedAt: '2026-04-13T17:00:00Z' }, error: null },
  description: { data: { summary: 'Migrate inter-service communication from synchronous REST calls to an event-driven architecture using Apache Kafka. This enables asynchronous processing, better fault isolation, and horizontal scalability.', businessJustification: 'Current synchronous architecture creates cascading failures under load. P1 incident INC-0421 (DB pool exhaustion) was caused by synchronous call chains. Event-driven decoupling prevents this class of failure.', acceptanceCriteria: [ { id: 'AC-070', text: 'Core business events are published to Kafka topics', isMet: false }, { id: 'AC-071', text: 'Consumers process events idempotently', isMet: false }, { id: 'AC-072', text: 'Dead letter queue handles failed messages', isMet: false } ], assumptions: ['Kafka cluster is provisioned', 'Schema registry is available for Avro/Protobuf schemas'], constraints: ['Must maintain REST APIs for external consumers', 'Migration must be incremental, not big-bang'] }, error: null },
  linkedStories: { data: { stories: [], totalCount: 0 }, error: null },
  linkedSpecs: { data: { specs: [], totalCount: 0 }, error: null },
  aiAnalysis: { data: null, error: null },
  sdlcChain: { data: { links: [ { artifactType: 'requirement', artifactId: 'REQ-0008', artifactTitle: 'Event-Driven Architecture Migration', routePath: '/requirements/REQ-0008' } ] }, error: null },
};

/**
 * Mock requirement detail for REQ-0009
 */
export const MOCK_REQUIREMENT_DETAIL_0009: RequirementDetail = {
  header: { data: { id: 'REQ-0009', title: 'Legacy Report Export', priority: 'Medium', status: 'Archived', category: 'Business', source: 'Imported', assignee: 'Alex Kim', createdAt: '2026-03-20T09:00:00Z', updatedAt: '2026-04-10T10:00:00Z' }, error: null },
  description: { data: { summary: 'Export SDLC metrics and compliance reports in PDF, CSV, and Excel formats compatible with legacy reporting tools used by finance and compliance teams.', businessJustification: 'Finance team requires monthly SDLC cost reports in Excel format compatible with their existing SAP integration. Compliance team needs PDF audit reports.', acceptanceCriteria: [ { id: 'AC-080', text: 'Reports export in PDF, CSV, and XLSX formats', isMet: true }, { id: 'AC-081', text: 'Export includes all required compliance fields', isMet: true } ], assumptions: ['Report templates are defined by finance team'], constraints: ['XLSX format must be compatible with Excel 2016+'] }, error: null },
  linkedStories: { data: { stories: [ { id: 'US-080', title: 'As a finance user, I can export monthly cost reports', status: 'Done', specId: 'SPEC-080', specStatus: 'Implemented' }, { id: 'US-081', title: 'As a compliance officer, I can generate PDF audit reports', status: 'Done', specId: null, specStatus: null } ], totalCount: 2 }, error: null },
  linkedSpecs: { data: { specs: [ { id: 'SPEC-080', title: 'Report Export API Specification', status: 'Implemented', version: 'v1.1' } ], totalCount: 1 }, error: null },
  aiAnalysis: { data: { completenessScore: 100, missingElements: [], similarRequirements: [], impactAssessment: 'Low — archived. Feature delivered and stable.', suggestions: [] }, error: null },
  sdlcChain: { data: { links: [ { artifactType: 'requirement', artifactId: 'REQ-0009', artifactTitle: 'Legacy Report Export', routePath: '/requirements/REQ-0009' }, { artifactType: 'code', artifactId: 'MR-1120', artifactTitle: 'feat: report export service', routePath: '/code' } ] }, error: null },
};

/**
 * Mock requirement detail for REQ-0010
 */
export const MOCK_REQUIREMENT_DETAIL_0010: RequirementDetail = {
  header: { data: { id: 'REQ-0010', title: 'AI-Powered Requirement Analysis', priority: 'High', status: 'In Review', category: 'Functional', source: 'AI-Generated', assignee: 'Sarah Chen', createdAt: '2026-04-15T07:00:00Z', updatedAt: '2026-04-16T07:00:00Z' }, error: null },
  description: { data: { summary: 'Integrate AI analysis capabilities into the requirement management module to automatically assess completeness, detect duplicates, estimate impact, and suggest improvements for incoming requirements.', businessJustification: 'Manual requirement review takes 2-3 hours per requirement. AI analysis can reduce this to 15 minutes by pre-screening completeness and flagging issues. Reduces requirement rework by an estimated 40%.', acceptanceCriteria: [ { id: 'AC-090', text: 'AI completeness scoring runs on requirement save', isMet: false }, { id: 'AC-091', text: 'Similar requirement detection uses vector similarity', isMet: false }, { id: 'AC-092', text: 'AI suggestions are actionable and editable', isMet: false } ], assumptions: ['LLM API is available with sufficient quota', 'Vector embeddings are stored for all existing requirements'], constraints: ['Analysis must complete within 30 seconds', 'AI results must be clearly labeled as suggestions, not authoritative'] }, error: null },
  linkedStories: { data: { stories: [ { id: 'US-090', title: 'As a product owner, I see AI analysis after saving a requirement', status: 'Draft', specId: null, specStatus: null }, { id: 'US-091', title: 'As a product owner, I can accept or dismiss AI suggestions', status: 'Draft', specId: null, specStatus: null } ], totalCount: 2 }, error: null },
  linkedSpecs: { data: { specs: [], totalCount: 0 }, error: null },
  aiAnalysis: { data: { completenessScore: 35, missingElements: ['LLM model selection not specified', 'Embedding dimension and storage strategy undefined', 'Confidence threshold for suggestions not defined'], similarRequirements: [{ id: 'REQ-0001', similarity: 30 }], impactAssessment: 'High — core differentiator for the product. Transforms requirement management from manual to AI-assisted.', suggestions: ['Define fallback behavior when LLM API is unavailable', 'Specify minimum confidence threshold for duplicate detection', 'Add user feedback loop to improve AI accuracy over time'] }, error: null },
  sdlcChain: { data: { links: [ { artifactType: 'requirement', artifactId: 'REQ-0010', artifactTitle: 'AI-Powered Requirement Analysis', routePath: '/requirements/REQ-0010' } ] }, error: null },
};

/**
 * Detail lookup by requirement ID — all 10 requirements covered
 */
export const MOCK_REQUIREMENT_DETAILS: Record<string, RequirementDetail> = {
  'REQ-0001': MOCK_REQUIREMENT_DETAIL_0001,
  'REQ-0002': MOCK_REQUIREMENT_DETAIL_0002,
  'REQ-0003': MOCK_REQUIREMENT_DETAIL_0003,
  'REQ-0004': MOCK_REQUIREMENT_DETAIL_0004,
  'REQ-0005': MOCK_REQUIREMENT_DETAIL_0005,
  'REQ-0006': MOCK_REQUIREMENT_DETAIL_0006,
  'REQ-0007': MOCK_REQUIREMENT_DETAIL_0007,
  'REQ-0008': MOCK_REQUIREMENT_DETAIL_0008,
  'REQ-0009': MOCK_REQUIREMENT_DETAIL_0009,
  'REQ-0010': MOCK_REQUIREMENT_DETAIL_0010,
};
