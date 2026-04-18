INSERT INTO design_artifact (
    id, workspace_id, project_id, title, format, lifecycle, current_version_id, registered_by_member_id, created_at, updated_at
) VALUES
    ('art-2041', 'ws-default-001', 'proj-42', 'Control Tower Dashboard', 'STITCH', 'PUBLISHED', NULL, 'u-007', TIMESTAMP WITH TIME ZONE '2026-04-11 08:00:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 08:20:00+00:00'),
    ('art-2042', 'ws-default-001', 'proj-11', 'Project Space Drilldown', 'HTML', 'PUBLISHED', NULL, 'u-003', TIMESTAMP WITH TIME ZONE '2026-04-10 07:30:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-16 09:40:00+00:00'),
    ('art-2043', 'ws-default-001', 'proj-55', 'Incident Command Center', 'STITCH', 'DRAFT', NULL, 'u-007', TIMESTAMP WITH TIME ZONE '2026-04-16 05:10:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-17 07:05:00+00:00'),
    ('art-2044', 'ws-default-001', 'proj-88', 'Platform Center Access Matrix', 'HTML', 'RETIRED', NULL, 'u-003', TIMESTAMP WITH TIME ZONE '2026-04-03 10:15:00+00:00', TIMESTAMP WITH TIME ZONE '2026-04-14 03:15:00+00:00');

INSERT INTO design_artifact_version (
    id, artifact_id, version_number, html_payload, html_size_bytes, content_sha256, changelog_note, created_by_member_id, created_at
) VALUES
    (
        'art-2041-v1',
        'art-2041',
        1,
        '<!doctype html><html><head><meta charset="utf-8"><script src="https://cdn.tailwindcss.com"></script><link rel="preconnect" href="https://fonts.googleapis.com"><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet"></head><body style="font-family: Inter, sans-serif; background:#07111f; color:#e6eefc;"><main class="p-8"><h1 class="text-3xl font-bold">Control Tower Dashboard</h1><p class="mt-3 text-slate-300">Initial dashboard shell with chain strip and AI panel slot.</p></main></body></html>',
        612,
        'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
        'Initial registration snapshot',
        'u-007',
        TIMESTAMP WITH TIME ZONE '2026-04-11 08:00:00+00:00'
    ),
    (
        'art-2041-v2',
        'art-2041',
        2,
        '<!doctype html><html><head><meta charset="utf-8"><script src="https://cdn.tailwindcss.com"></script><link rel="preconnect" href="https://fonts.googleapis.com"><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet"></head><body style="font-family: Inter, sans-serif; background:#07111f; color:#e6eefc;"><main class="p-8"><h1 class="text-3xl font-bold">Control Tower Dashboard</h1><p class="mt-3 text-slate-300">Latest dashboard mock with SDLC chain strip, incident counters, and command brief.</p><div class="mt-6 grid grid-cols-3 gap-4"><section class="rounded-2xl bg-slate-800/80 p-4">Chain strip</section><section class="rounded-2xl bg-slate-800/80 p-4">Incident counters</section><section class="rounded-2xl bg-slate-800/80 p-4">AI command brief</section></div></main></body></html>',
        864,
        'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
        'Aligned shell density and command panel placement',
        'u-003',
        TIMESTAMP WITH TIME ZONE '2026-04-17 08:20:00+00:00'
    ),
    (
        'art-2042-v1',
        'art-2042',
        1,
        '<!doctype html><html><head><meta charset="utf-8"><script src="https://cdn.tailwindcss.com"></script></head><body style="background:#0f172a;color:#e2e8f0;"><main class="p-8"><h1 class="text-3xl font-bold">Project Space Drilldown</h1><p class="mt-4 text-slate-300">Milestones, environments, and dependencies grouped by execution lane.</p><div class="mt-6 grid grid-cols-2 gap-4"><article class="rounded-xl bg-slate-800/80 p-4">Milestones</article><article class="rounded-xl bg-slate-800/80 p-4">Dependencies</article></div></main></body></html>',
        574,
        'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc',
        'Published for walkthrough review',
        'u-003',
        TIMESTAMP WITH TIME ZONE '2026-04-16 09:40:00+00:00'
    ),
    (
        'art-2043-v1',
        'art-2043',
        1,
        '<!doctype html><html><head><meta charset="utf-8"><script src="https://cdn.tailwindcss.com"></script></head><body style="background:#08111d;color:#dbeafe;"><main class="p-8"><h1 class="text-3xl font-bold">Incident Command Center</h1><p class="mt-4 text-slate-300">Draft workbench for severity framing, timeline, and escalation routing.</p></main></body></html>',
        402,
        'dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd',
        'Draft imported from Stitch for review',
        'u-007',
        TIMESTAMP WITH TIME ZONE '2026-04-17 07:05:00+00:00'
    ),
    (
        'art-2044-v1',
        'art-2044',
        1,
        '<!doctype html><html><head><meta charset="utf-8"><script src="https://cdn.tailwindcss.com"></script></head><body style="background:#101827;color:#f8fafc;"><main class="p-8"><h1 class="text-3xl font-bold">Platform Center Access Matrix</h1><p class="mt-4 text-slate-300">Historical access governance layout retained for audit reference only.</p></main></body></html>',
        398,
        'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee',
        'Final archived reference before retirement',
        'u-003',
        TIMESTAMP WITH TIME ZONE '2026-04-14 03:15:00+00:00'
    );

UPDATE design_artifact SET current_version_id = 'art-2041-v2' WHERE id = 'art-2041';
UPDATE design_artifact SET current_version_id = 'art-2042-v1' WHERE id = 'art-2042';
UPDATE design_artifact SET current_version_id = 'art-2043-v1' WHERE id = 'art-2043';
UPDATE design_artifact SET current_version_id = 'art-2044-v1' WHERE id = 'art-2044';

INSERT INTO design_artifact_author (artifact_id, member_id) VALUES
    ('art-2041', 'u-007'),
    ('art-2041', 'u-003'),
    ('art-2042', 'u-003'),
    ('art-2042', 'u-011'),
    ('art-2043', 'u-007'),
    ('art-2044', 'u-003');

INSERT INTO design_spec_link (
    id, artifact_id, spec_id, covers_revision, declared_coverage, linked_by_member_id, linked_at
) VALUES
    ('dsl-2041-1', 'art-2041', 'SPEC-001', 9, 'FULL', 'u-007', TIMESTAMP WITH TIME ZONE '2026-04-17 08:22:00+00:00'),
    ('dsl-2041-2', 'art-2041', 'SPEC-040', 2, 'FULL', 'u-007', TIMESTAMP WITH TIME ZONE '2026-04-17 08:22:30+00:00'),
    ('dsl-2042-1', 'art-2042', 'SPEC-010', 5, 'FULL', 'u-003', TIMESTAMP WITH TIME ZONE '2026-04-16 09:42:00+00:00'),
    ('dsl-2042-2', 'art-2042', 'SPEC-080', 1, 'PARTIAL', 'u-003', TIMESTAMP WITH TIME ZONE '2026-04-16 09:42:30+00:00'),
    ('dsl-2044-1', 'art-2044', 'SPEC-060', 1, 'FULL', 'u-003', TIMESTAMP WITH TIME ZONE '2026-04-10 10:00:00+00:00');

INSERT INTO design_ai_summary (
    id, artifact_id, version_id, skill_version, status, payload_json, error_json, generated_at
) VALUES
    (
        'dais-2041-v2',
        'art-2041',
        'art-2041-v2',
        'artifact-summarizer@1.0.0',
        'SUCCESS',
        '{"summaryText":"Dashboard mock emphasizing SDLC chain status, incident counters, and an AI operations brief.","keyElements":["SDLC chain strip","Incident counter group","AI command brief"]}',
        NULL,
        TIMESTAMP WITH TIME ZONE '2026-04-17 08:23:00+00:00'
    ),
    (
        'dais-2042-v1',
        'art-2042',
        'art-2042-v1',
        'artifact-summarizer@1.0.0',
        'SUCCESS',
        '{"summaryText":"Project execution workspace mock grouping milestones, environments, and dependency negotiation context.","keyElements":["Milestone lane","Dependency lane","Execution summary"]}',
        NULL,
        TIMESTAMP WITH TIME ZONE '2026-04-16 09:43:00+00:00'
    ),
    (
        'dais-2044-v1',
        'art-2044',
        'art-2044-v1',
        'artifact-summarizer@1.0.0',
        'FAILED',
        NULL,
        '{"message":"Source artifact retired before regeneration completed."}',
        TIMESTAMP WITH TIME ZONE '2026-04-14 03:20:00+00:00'
    );

INSERT INTO design_change_log (
    id, artifact_id, entry_type, actor_member_id, actor_skill_execution_id, before_json, after_json, reason, correlation_id, occurred_at
) VALUES
    ('dlog-2041-01', 'art-2041', 'REGISTERED', 'u-007', NULL, NULL, '{"title":"Control Tower Dashboard"}', 'Artifact registered', 'corr-2041-01', TIMESTAMP WITH TIME ZONE '2026-04-11 08:00:00+00:00'),
    ('dlog-2041-02', 'art-2041', 'VERSION_PUBLISHED', 'u-007', NULL, NULL, '{"versionId":"art-2041-v1"}', 'Initial version published', 'corr-2041-02', TIMESTAMP WITH TIME ZONE '2026-04-11 08:00:00+00:00'),
    ('dlog-2041-03', 'art-2041', 'VERSION_PUBLISHED', 'u-003', NULL, '{"versionId":"art-2041-v1"}', '{"versionId":"art-2041-v2"}', 'Refined dashboard density', 'corr-2041-03', TIMESTAMP WITH TIME ZONE '2026-04-17 08:20:00+00:00'),
    ('dlog-2041-04', 'art-2041', 'SPEC_LINKED', 'u-007', NULL, NULL, '{"specId":"SPEC-001"}', 'Primary authentication spec linked', 'corr-2041-04', TIMESTAMP WITH TIME ZONE '2026-04-17 08:22:00+00:00'),
    ('dlog-2041-05', 'art-2041', 'AI_SUMMARY_REGENERATED', 'u-007', NULL, NULL, '{"summaryId":"dais-2041-v2"}', 'AI summary refreshed after v2 publish', 'corr-2041-05', TIMESTAMP WITH TIME ZONE '2026-04-17 08:23:00+00:00'),
    ('dlog-2042-01', 'art-2042', 'REGISTERED', 'u-003', NULL, NULL, '{"title":"Project Space Drilldown"}', 'Artifact registered', 'corr-2042-01', TIMESTAMP WITH TIME ZONE '2026-04-16 09:40:00+00:00'),
    ('dlog-2042-02', 'art-2042', 'SPEC_LINKED', 'u-003', NULL, NULL, '{"specId":"SPEC-010"}', 'RBAC spec linked', 'corr-2042-02', TIMESTAMP WITH TIME ZONE '2026-04-16 09:42:00+00:00'),
    ('dlog-2043-01', 'art-2043', 'REGISTERED', 'u-007', NULL, NULL, '{"title":"Incident Command Center"}', 'Draft imported for review', 'corr-2043-01', TIMESTAMP WITH TIME ZONE '2026-04-17 07:05:00+00:00'),
    ('dlog-2044-01', 'art-2044', 'RETIRED', 'u-003', NULL, '{"lifecycle":"PUBLISHED"}', '{"lifecycle":"RETIRED"}', 'Superseded by access governance refresh', 'corr-2044-01', TIMESTAMP WITH TIME ZONE '2026-04-14 03:15:00+00:00');
