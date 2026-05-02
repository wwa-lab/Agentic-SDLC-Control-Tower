-- MTF-1: Add profile_id to PLATFORM_WORKSPACE, rename default workspace key,
-- and seed the IBM-i workspace with sample data for Day 1 acceptance demo.

-- Add SDD profile binding column to workspace (drives CLI agent profile selection).
ALTER TABLE PLATFORM_WORKSPACE ADD profile_id VARCHAR(64);

-- Set existing default workspace profile.
UPDATE PLATFORM_WORKSPACE
   SET workspace_key = 'payment-gateway-pro',
       name          = 'Payment Gateway Pro',
       profile_id    = 'standard-java-sdd'
 WHERE id = 'ws-default-001';

-- IBM-i SNOW group
INSERT INTO PLATFORM_SNOW_GROUP (id, servicenow_group_name, display_name, owner_email, escalation_policy, status, created_at, updated_at)
VALUES ('snow-ibmi-ops', 'IBMi-Operations', 'IBM-i Operations', 'ibmi-ops@example.com', 'p1-15min', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- IBM-i Application
INSERT INTO PLATFORM_APPLICATION (id, app_key, name, owner_snow_group_id, criticality, status, created_at, updated_at)
VALUES ('app-ibmi-core', 'ibmi-core', 'IBM-i Core', 'snow-ibmi-ops', 'critical', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- IBM-i Workspace (profile_id drives ibm-i SDD skill chain in CLI agent)
INSERT INTO PLATFORM_WORKSPACE (id, workspace_key, name, application_id, snow_group_id, profile_id, status, created_at, updated_at)
VALUES ('ws-ibm-i-team', 'ibm-i-team', 'IBM-i Team', 'app-ibmi-core', 'snow-ibmi-ops', 'ibm-i', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- IBM-i demo user
INSERT INTO PLATFORM_USER (staff_id, display_name, staff_name, email, profile_source, status, password_hash, created_at, updated_at)
VALUES ('43929999', 'IBM-i Demo Lead', 'IBM-i Demo Lead', 'ibmi-demo@example.com', 'manual', 'active', 'local-dev-placeholder-hash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Grant IBM-i user membership in IBM-i workspace
INSERT INTO PLATFORM_ROLE_ASSIGNMENT (id, staff_id, user_display_name, role, scope_type, scope_id, granted_by, granted_at)
VALUES ('ra-43929999-ws-ibm-i-team', '43929999', 'IBM-i Demo Lead', 'WORKSPACE_MEMBER', 'workspace', 'ws-ibm-i-team', 'system', CURRENT_TIMESTAMP);

-- Sample requirement in IBM-i workspace (Day 1 acceptance target)
INSERT INTO requirement (id, title, summary, business_justification, priority, status, category, source,
                         completeness_score, created_at, updated_at, workspace_id)
VALUES ('REQ-IBMI-0001',
        'Add new column to ORDLOG file for partial shipments',
        'Track partial shipments per order line in the ORDLOG physical file.',
        'Compliance with new logistics regulation effective Q3 2026.',
        'high', 'normalizing', 'enhancement', 'jira', 35,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ws-ibm-i-team');

-- Sample Jira source reference for REQ-IBMI-0001
INSERT INTO requirement_source_reference (id, requirement_id, source_type, external_id, title, url,
                                          freshness_status, created_at, workspace_id)
VALUES ('src-ibmi-jira-001', 'REQ-IBMI-0001', 'JIRA', 'IBMI-1234',
        'IBMI-1234 Partial shipment tracking',
        'https://jira.example.com/browse/IBMI-1234',
        'FRESH', CURRENT_TIMESTAMP, 'ws-ibm-i-team');
