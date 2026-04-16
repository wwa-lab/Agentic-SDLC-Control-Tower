import type { IncidentList, IncidentDetail } from './types/incident';

/**
 * Mock incident list — matches API guide §2.2
 */
export const MOCK_INCIDENT_LIST: IncidentList = {
  severityDistribution: { p1: 1, p2: 2, p3: 2, p4: 0 },
  incidents: [
    {
      id: 'INC-0422',
      title: 'API Gateway Latency Spike (>500ms)',
      priority: 'P1',
      status: 'PENDING_APPROVAL',
      handlerType: 'AI',
      controlMode: 'Approval',
      detectedAt: '2026-04-16T09:40:00Z',
      duration: 'PT1H23M',
    },
    {
      id: 'INC-0421',
      title: 'Database Connection Pool Exhaustion',
      priority: 'P2',
      status: 'EXECUTING',
      handlerType: 'AI',
      controlMode: 'Approval',
      detectedAt: '2026-04-16T08:15:00Z',
      duration: 'PT2H48M',
    },
    {
      id: 'INC-0420',
      title: 'Cache Miss Rate Spike in Product Service',
      priority: 'P3',
      status: 'RESOLVED',
      handlerType: 'Hybrid',
      controlMode: 'Manual',
      detectedAt: '2026-04-15T14:30:00Z',
      duration: 'PT4H12M',
    },
    {
      id: 'INC-0419',
      title: 'Certificate Expiry Warning (7 days)',
      priority: 'P3',
      status: 'CLOSED',
      handlerType: 'AI',
      controlMode: 'Auto',
      detectedAt: '2026-04-14T10:00:00Z',
      duration: 'PT0H45M',
    },
    {
      id: 'INC-0418',
      title: 'Memory Leak in Notification Worker',
      priority: 'P2',
      status: 'LEARNING',
      handlerType: 'AI',
      controlMode: 'Approval',
      detectedAt: '2026-04-13T16:20:00Z',
      duration: 'PT6H30M',
    },
  ],
};

/**
 * Mock incident detail for INC-0422 — matches API guide §3.2
 */
export const MOCK_INCIDENT_DETAIL_0422: IncidentDetail = {
  header: {
    data: {
      id: 'INC-0422',
      title: 'API Gateway Latency Spike (>500ms)',
      priority: 'P1',
      status: 'PENDING_APPROVAL',
      handlerType: 'AI',
      controlMode: 'Approval',
      autonomyLevel: 'Level2_SuggestApprove',
      detectedAt: '2026-04-16T09:40:00Z',
      acknowledgedAt: '2026-04-16T09:40:02Z',
      resolvedAt: null,
      duration: 'PT1H23M',
    },
    error: null,
  },
  diagnosis: {
    data: {
      entries: [
        { timestamp: '09:41:02', text: 'Analyzing k8s ingress logs for anomalous patterns...', entryType: 'analysis' },
        { timestamp: '09:41:05', text: 'Detected 3x increase in p99 latency starting at 09:38', entryType: 'finding' },
        { timestamp: '09:41:08', text: 'Correlating with recent deployment DEP-041 (v2.4.0) at 09:35', entryType: 'analysis' },
        { timestamp: '09:41:12', text: 'Pattern identified: SSL handshake timeout causing connection queuing', entryType: 'finding' },
        { timestamp: '09:41:15', text: 'Root cause: New TLS config in v2.4.0 increases handshake time by 300ms under load', entryType: 'conclusion' },
        { timestamp: '09:41:18', text: 'SUGGESTION: Scale pod replicas from 3 to 5 to absorb load while TLS config is patched', entryType: 'suggestion' },
      ],
      rootCause: {
        hypothesis: 'TLS configuration change in deployment v2.4.0 causes SSL handshake timeout under load, queuing connections at the ingress layer',
        confidence: 'High',
      },
      affectedComponents: ['api-gateway', 'ingress-controller', 'product-service'],
    },
    error: null,
  },
  skillTimeline: {
    data: {
      executions: [
        {
          skillName: 'incident-detection',
          startTime: '2026-04-16T09:40:00Z',
          endTime: '2026-04-16T09:40:02Z',
          status: 'completed',
          inputSummary: 'Anomaly signal: p99 latency > 500ms on api-gateway',
          outputSummary: 'Incident INC-0422 created, priority P1',
        },
        {
          skillName: 'incident-correlation',
          startTime: '2026-04-16T09:40:03Z',
          endTime: '2026-04-16T09:40:08Z',
          status: 'completed',
          inputSummary: 'INC-0422 signals + recent change log',
          outputSummary: 'Correlated with DEP-041 (v2.4.0) deployed at 09:35',
        },
        {
          skillName: 'incident-diagnosis',
          startTime: '2026-04-16T09:41:00Z',
          endTime: '2026-04-16T09:41:15Z',
          status: 'completed',
          inputSummary: 'INC-0422 + correlation data + ingress logs',
          outputSummary: 'Root cause: TLS config change causing handshake timeout',
        },
        {
          skillName: 'incident-remediation',
          startTime: '2026-04-16T09:41:16Z',
          endTime: null,
          status: 'pending_approval',
          inputSummary: 'Root cause + affected components',
          outputSummary: 'Proposed: Scale replicas 3→5 (requires approval per policy)',
        },
      ],
    },
    error: null,
  },
  actions: {
    data: {
      actions: [
        {
          id: 'ACT-001',
          description: 'Scale api-gateway pod replicas from 3 to 5',
          actionType: 'requires_approval',
          executionStatus: 'pending',
          timestamp: '2026-04-16T09:41:18Z',
          impactAssessment: 'Increases resource allocation by 67%; absorbs current load spike; no downtime',
          isRollbackable: true,
          policyRef: 'POL-003: Infrastructure scaling requires approval for >50% capacity change',
        },
      ],
    },
    error: null,
  },
  governance: {
    data: { entries: [] },
    error: null,
  },
  sdlcChain: {
    data: {
      links: [
        { artifactType: 'spec', artifactId: 'SPEC-089', artifactTitle: 'API Gateway TLS Configuration Spec', routePath: '/requirements' },
        { artifactType: 'code', artifactId: 'MR-1234', artifactTitle: 'feat: update TLS config for mTLS support', routePath: '/code' },
        { artifactType: 'deploy', artifactId: 'DEP-041', artifactTitle: 'Release v2.4.0 to production', routePath: '/deployment' },
      ],
    },
    error: null,
  },
  learning: {
    data: null,
    error: null,
  },
};

/**
 * Mock detail for INC-0420 (resolved, with learning)
 */
export const MOCK_INCIDENT_DETAIL_0420: IncidentDetail = {
  header: {
    data: {
      id: 'INC-0420',
      title: 'Cache Miss Rate Spike in Product Service',
      priority: 'P3',
      status: 'RESOLVED',
      handlerType: 'Hybrid',
      controlMode: 'Manual',
      autonomyLevel: 'Level1_Manual',
      detectedAt: '2026-04-15T14:30:00Z',
      acknowledgedAt: '2026-04-15T14:32:00Z',
      resolvedAt: '2026-04-15T18:42:00Z',
      duration: 'PT4H12M',
    },
    error: null,
  },
  diagnosis: {
    data: {
      entries: [
        { timestamp: '14:31:00', text: 'Analyzing Redis cache hit/miss ratios...', entryType: 'analysis' },
        { timestamp: '14:31:08', text: 'Cache miss rate increased from 5% to 42% at 14:28', entryType: 'finding' },
        { timestamp: '14:31:15', text: 'SUGGESTION: Flush stale cache keys and increase TTL for product catalog', entryType: 'suggestion' },
      ],
      rootCause: {
        hypothesis: 'Bulk product catalog update invalidated cache without warming',
        confidence: 'Medium',
      },
      affectedComponents: ['product-service', 'redis-cluster'],
    },
    error: null,
  },
  skillTimeline: {
    data: {
      executions: [
        { skillName: 'incident-detection', startTime: '2026-04-15T14:30:00Z', endTime: '2026-04-15T14:30:05Z', status: 'completed', inputSummary: 'Cache miss alert threshold breached', outputSummary: 'INC-0420 created, P3' },
        { skillName: 'incident-diagnosis', startTime: '2026-04-15T14:31:00Z', endTime: '2026-04-15T14:31:15Z', status: 'completed', inputSummary: 'Redis metrics + change log', outputSummary: 'Bulk catalog update identified as trigger' },
      ],
    },
    error: null,
  },
  actions: {
    data: {
      actions: [
        { id: 'ACT-010', description: 'Flush stale product cache keys', actionType: 'requires_approval', executionStatus: 'executed', timestamp: '2026-04-15T14:35:00Z', impactAssessment: 'Brief 2-3s latency spike during cache rebuild', isRollbackable: false, policyRef: null },
      ],
    },
    error: null,
  },
  governance: {
    data: {
      entries: [
        { actor: 'sarah.chen', timestamp: '2026-04-15T14:34:00Z', actionTaken: 'approve', reason: null, policyRef: null },
        { actor: 'sarah.chen', timestamp: '2026-04-15T18:40:00Z', actionTaken: 'override', reason: 'Manual cache warming applied; closing via override', policyRef: null },
      ],
    },
    error: null,
  },
  sdlcChain: {
    data: {
      links: [
        { artifactType: 'code', artifactId: 'MR-1201', artifactTitle: 'feat: bulk product catalog sync', routePath: '/code' },
      ],
    },
    error: null,
  },
  learning: {
    data: {
      rootCause: 'Bulk catalog update invalidated 12,000 cache keys simultaneously without warming',
      patternIdentified: 'Large-scale cache invalidation without pre-warming causes cascading miss spikes',
      preventionRecommendations: [
        'Implement cache warming step in bulk update pipeline',
        'Add circuit breaker for cache miss rate > 30%',
        'Create runbook for manual cache warming procedure',
      ],
      knowledgeBaseEntryCreated: true,
    },
    error: null,
  },
};

/**
 * Mock detail for INC-0421 (executing, AI)
 */
export const MOCK_INCIDENT_DETAIL_0421: IncidentDetail = {
  header: {
    data: {
      id: 'INC-0421', title: 'Database Connection Pool Exhaustion', priority: 'P2',
      status: 'EXECUTING', handlerType: 'AI', controlMode: 'Approval',
      autonomyLevel: 'Level2_SuggestApprove', detectedAt: '2026-04-16T08:15:00Z',
      acknowledgedAt: '2026-04-16T08:15:03Z', resolvedAt: null, duration: 'PT2H48M',
    }, error: null,
  },
  diagnosis: {
    data: {
      entries: [
        { timestamp: '08:16:00', text: 'Analyzing HikariCP pool metrics...', entryType: 'analysis' },
        { timestamp: '08:16:05', text: 'Active connections at 100% capacity (50/50)', entryType: 'finding' },
        { timestamp: '08:16:10', text: 'Long-running queries detected in order-service', entryType: 'finding' },
        { timestamp: '08:16:15', text: 'SUGGESTION: Kill idle connections and increase pool max to 80', entryType: 'suggestion' },
      ],
      rootCause: { hypothesis: 'Long-running queries from batch job saturated connection pool', confidence: 'Medium' },
      affectedComponents: ['order-service', 'postgres-primary'],
    }, error: null,
  },
  skillTimeline: {
    data: { executions: [
      { skillName: 'incident-detection', startTime: '2026-04-16T08:15:00Z', endTime: '2026-04-16T08:15:03Z', status: 'completed', inputSummary: 'Connection pool exhaustion alert', outputSummary: 'INC-0421 created, P2' },
      { skillName: 'incident-diagnosis', startTime: '2026-04-16T08:16:00Z', endTime: '2026-04-16T08:16:15Z', status: 'completed', inputSummary: 'Pool metrics + query log', outputSummary: 'Batch job causing pool saturation' },
      { skillName: 'incident-remediation', startTime: '2026-04-16T08:17:00Z', endTime: '2026-04-16T08:17:30Z', status: 'completed', inputSummary: 'Kill idle + resize pool', outputSummary: 'Executing approved remediation' },
    ] }, error: null,
  },
  actions: { data: { actions: [
    { id: 'ACT-002', description: 'Increase connection pool max from 50 to 80', actionType: 'requires_approval', executionStatus: 'executing', timestamp: '2026-04-16T08:17:00Z', impactAssessment: 'Temporary pool increase; monitor for 1h then reassess', isRollbackable: true, policyRef: null },
  ] }, error: null },
  governance: { data: { entries: [
    { actor: 'alex.kim', timestamp: '2026-04-16T08:16:45Z', actionTaken: 'approve', reason: null, policyRef: null },
  ] }, error: null },
  sdlcChain: { data: { links: [
    { artifactType: 'code', artifactId: 'MR-1198', artifactTitle: 'feat: batch order reconciliation job', routePath: '/code' },
  ] }, error: null },
  learning: { data: null, error: null },
};

/**
 * Mock detail for INC-0419 (closed, AI, with learning)
 */
export const MOCK_INCIDENT_DETAIL_0419: IncidentDetail = {
  header: {
    data: {
      id: 'INC-0419', title: 'Certificate Expiry Warning (7 days)', priority: 'P3',
      status: 'CLOSED', handlerType: 'AI', controlMode: 'Auto',
      autonomyLevel: 'Level3_AutoAudit', detectedAt: '2026-04-14T10:00:00Z',
      acknowledgedAt: '2026-04-14T10:00:01Z', resolvedAt: '2026-04-14T10:45:00Z', duration: 'PT0H45M',
    }, error: null,
  },
  diagnosis: {
    data: {
      entries: [
        { timestamp: '10:00:05', text: 'Certificate expiry scan triggered', entryType: 'analysis' },
        { timestamp: '10:00:10', text: 'api-gateway.example.com cert expires in 7 days', entryType: 'finding' },
        { timestamp: '10:00:15', text: 'Auto-renewing via Let\'s Encrypt ACME flow', entryType: 'conclusion' },
      ],
      rootCause: { hypothesis: 'Certificate approaching expiry; auto-renewal initiated', confidence: 'High' },
      affectedComponents: ['api-gateway'],
    }, error: null,
  },
  skillTimeline: {
    data: { executions: [
      { skillName: 'incident-detection', startTime: '2026-04-14T10:00:00Z', endTime: '2026-04-14T10:00:01Z', status: 'completed', inputSummary: 'Cert expiry monitoring', outputSummary: 'INC-0419 created, P3' },
      { skillName: 'incident-remediation', startTime: '2026-04-14T10:00:15Z', endTime: '2026-04-14T10:44:00Z', status: 'completed', inputSummary: 'ACME renewal flow', outputSummary: 'Certificate renewed successfully' },
    ] }, error: null,
  },
  actions: { data: { actions: [
    { id: 'ACT-020', description: 'Auto-renew certificate via ACME', actionType: 'automated', executionStatus: 'executed', timestamp: '2026-04-14T10:00:15Z', impactAssessment: 'Zero-downtime cert rotation', isRollbackable: false, policyRef: null },
  ] }, error: null },
  governance: { data: { entries: [] }, error: null },
  sdlcChain: { data: { links: [] }, error: null },
  learning: {
    data: {
      rootCause: 'Certificate nearing expiry due to renewal job being paused during maintenance window',
      patternIdentified: 'Maintenance windows can block automated cert renewal',
      preventionRecommendations: ['Exclude cert renewal from maintenance freeze windows', 'Add 14-day early warning threshold instead of 7 days'],
      knowledgeBaseEntryCreated: true,
    }, error: null,
  },
};

/**
 * Mock detail for INC-0418 (learning, AI)
 */
export const MOCK_INCIDENT_DETAIL_0418: IncidentDetail = {
  header: {
    data: {
      id: 'INC-0418', title: 'Memory Leak in Notification Worker', priority: 'P2',
      status: 'LEARNING', handlerType: 'AI', controlMode: 'Approval',
      autonomyLevel: 'Level2_SuggestApprove', detectedAt: '2026-04-13T16:20:00Z',
      acknowledgedAt: '2026-04-13T16:20:05Z', resolvedAt: '2026-04-13T22:50:00Z', duration: 'PT6H30M',
    }, error: null,
  },
  diagnosis: {
    data: {
      entries: [
        { timestamp: '16:21:00', text: 'Analyzing heap dump from notification-worker pod...', entryType: 'analysis' },
        { timestamp: '16:21:15', text: 'Heap usage growing linearly: 2GB/hr, OOM projected in 4h', entryType: 'finding' },
        { timestamp: '16:21:30', text: 'Leak traced to unclosed WebSocket connections in retry loop', entryType: 'conclusion' },
        { timestamp: '16:21:35', text: 'SUGGESTION: Restart worker pod and deploy fix from MR-1210', entryType: 'suggestion' },
      ],
      rootCause: { hypothesis: 'WebSocket connections not closed in notification retry loop', confidence: 'High' },
      affectedComponents: ['notification-worker'],
    }, error: null,
  },
  skillTimeline: {
    data: { executions: [
      { skillName: 'incident-detection', startTime: '2026-04-13T16:20:00Z', endTime: '2026-04-13T16:20:05Z', status: 'completed', inputSummary: 'Memory usage alert > 80%', outputSummary: 'INC-0418 created, P2' },
      { skillName: 'incident-diagnosis', startTime: '2026-04-13T16:21:00Z', endTime: '2026-04-13T16:21:35Z', status: 'completed', inputSummary: 'Heap dump analysis', outputSummary: 'WebSocket leak in retry loop' },
      { skillName: 'incident-remediation', startTime: '2026-04-13T16:25:00Z', endTime: '2026-04-13T16:26:00Z', status: 'completed', inputSummary: 'Restart pod + deploy fix', outputSummary: 'Worker restarted, fix deployed' },
      { skillName: 'incident-learning', startTime: '2026-04-13T22:50:00Z', endTime: null, status: 'running', inputSummary: 'Post-incident analysis', outputSummary: 'Generating prevention recommendations' },
    ] }, error: null,
  },
  actions: { data: { actions: [
    { id: 'ACT-030', description: 'Restart notification-worker pod', actionType: 'requires_approval', executionStatus: 'executed', timestamp: '2026-04-13T16:25:00Z', impactAssessment: 'Service interruption for ~30s during restart', isRollbackable: true, policyRef: null },
    { id: 'ACT-031', description: 'Deploy hotfix MR-1210', actionType: 'requires_approval', executionStatus: 'executed', timestamp: '2026-04-13T16:25:30Z', impactAssessment: 'Fixes WebSocket connection leak; rolling deploy', isRollbackable: false, policyRef: null },
  ] }, error: null },
  governance: { data: { entries: [
    { actor: 'mike.ross', timestamp: '2026-04-13T16:24:00Z', actionTaken: 'approve', reason: null, policyRef: null },
  ] }, error: null },
  sdlcChain: { data: { links: [
    { artifactType: 'code', artifactId: 'MR-1210', artifactTitle: 'fix: close WebSocket connections in retry loop', routePath: '/code' },
  ] }, error: null },
  learning: { data: null, error: null },
};

/**
 * Detail lookup by incident ID — all 5 incidents covered
 */
export const MOCK_INCIDENT_DETAILS: Record<string, IncidentDetail> = {
  'INC-0422': MOCK_INCIDENT_DETAIL_0422,
  'INC-0421': MOCK_INCIDENT_DETAIL_0421,
  'INC-0420': MOCK_INCIDENT_DETAIL_0420,
  'INC-0419': MOCK_INCIDENT_DETAIL_0419,
  'INC-0418': MOCK_INCIDENT_DETAIL_0418,
};
