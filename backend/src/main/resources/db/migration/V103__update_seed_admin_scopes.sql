-- MTF-1: Ensure the seed admin (43910516) has WORKSPACE_MEMBER on both seeded workspaces.
-- Uses MERGE so re-running is idempotent.

MERGE INTO PLATFORM_ROLE_ASSIGNMENT t
USING (VALUES
    ('ra-seed-admin-member-ws-default-001', '43910516', 'Platform Admin', 'WORKSPACE_MEMBER', 'workspace', 'ws-default-001', 'system', CURRENT_TIMESTAMP),
    ('ra-seed-admin-member-ws-ibm-i-team',  '43910516', 'Platform Admin', 'WORKSPACE_MEMBER', 'workspace', 'ws-ibm-i-team',  'system', CURRENT_TIMESTAMP)
) s (id, staff_id, user_display_name, role, scope_type, scope_id, granted_by, granted_at)
ON t.id = s.id
WHEN NOT MATCHED THEN
    INSERT (id, staff_id, user_display_name, role, scope_type, scope_id, granted_by, granted_at)
    VALUES (s.id, s.staff_id, s.user_display_name, s.role, s.scope_type, s.scope_id, s.granted_by, s.granted_at);
