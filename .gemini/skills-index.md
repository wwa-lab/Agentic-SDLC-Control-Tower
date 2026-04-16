# Gemini Skills Index

This file maps Gemini-readable workflows to the existing Claude skill library in this repo.

## Usage Rule

When you need one of these workflows, open the linked `SKILL.md` file and follow it as the
authoritative instruction set.

## Available Skills

### `req-to-user-story`
- Source: `.claude/skills/req-to-user-story/SKILL.md`
- Use when:
  the user provides raw requirements, feature ideas, meeting notes, or asks for user stories
- Produces:
  structured Agile user stories with acceptance criteria, assumptions, dependencies, and open questions
- Default output:
  `docs/02-user-stories/user-stories.md`

### `user-story-to-spec`
- Source: `.claude/skills/user-story-to-spec/SKILL.md`
- Use when:
  the user provides stories and needs a structured engineering specification
- Produces:
  `spec.md`-style engineering specification for architecture and implementation planning
- Default output:
  `docs/03-spec/spec.md`

### `spec-to-architecture`
- Source: `.claude/skills/spec-to-architecture/SKILL.md`
- Use when:
  the user provides a spec or requirements doc and needs a system architecture
- Produces:
  high-level architecture document suitable for technical review and downstream planning
- Default output:
  `docs/04-architecture/architecture.md`

### `architecture-to-design`
- Source: `.claude/skills/architecture-to-design/SKILL.md`
- Use when:
  the user asks for a design, detailed design, implementation design, or design artifacts from a spec or architecture
- Produces:
  design document, architecture refinements, data model, and API implementation guidance
- Default outputs:
  `docs/05-design/design.md`
  `docs/04-architecture/data-model.md`
  `docs/05-design/contracts/API_IMPLEMENTATION_GUIDE.md`

### `design-to-tasks`
- Source: `.claude/skills/design-to-tasks/SKILL.md`
- Use when:
  the user asks for engineering task breakdown, sprint planning, or Jira decomposition from a design
- Produces:
  structured execution tasks, dependencies, risks, and implementation sequencing
- Default output:
  `docs/06-tasks/tasks.md`

### `tasks-to-code`
- Source: `.claude/skills/tasks-to-code/SKILL.md`
- Use when:
  the repo already exists and the user wants incremental feature work implemented from tasks
- Produces:
  working code changes, tests, and supporting implementation artifacts
- Default output:
  repository code changes

### `tasks-to-implementation`
- Source: `.claude/skills/tasks-to-implementation/SKILL.md`
- Use when:
  the user needs greenfield scaffolding, brownfield implementation-mode selection, or migration work
- Produces:
  working implementation across greenfield, brownfield, or migration modes
- Default output:
  repository code and scaffolding changes

### `review-doc-quality`
- Source: `.claude/skills/review-doc-quality/SKILL.md`
- Use when:
  the user wants a doc reviewed for completeness, coherence, traceability, and readiness for handoff
- Produces:
  a structured quality review with gaps, verdict, and handoff readiness
- Default output:
  review comments or a quality report

### `review-code-against-design`
- Source: `.claude/skills/review-code-against-design/SKILL.md`
- Use when:
  the user wants to verify whether implementation matches design, tasks, or architectural intent
- Produces:
  a design-compliance review focused on implementation fidelity
- Default output:
  review findings

## Stage Mapping

Use this sequence when the user is moving left-to-right through the SDLC:

1. `req-to-user-story`
2. `user-story-to-spec`
3. `spec-to-architecture`
4. `architecture-to-design`
5. `design-to-tasks`
6. `tasks-to-code` or `tasks-to-implementation`

Use review skills before any downstream handoff.
