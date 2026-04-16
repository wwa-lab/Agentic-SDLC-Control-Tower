# Requirement Management Design

## Purpose

This document defines the concrete component APIs, file structure, data model,
visual design decisions, and API contracts for the Requirement Management page.
It bridges the [architecture](../04-architecture/requirement-architecture.md) and the
[implementation tasks](../06-tasks/requirement-tasks.md).

## Traceability

- Requirements: [requirement-requirements.md](../01-requirements/requirement-requirements.md)
- Stories: [requirement-stories.md](../02-user-stories/requirement-stories.md)
- Spec: [requirement-spec.md](../03-spec/requirement-spec.md)
- Architecture: [requirement-architecture.md](../04-architecture/requirement-architecture.md)
- Data Model: [requirement-data-model.md](../04-architecture/requirement-data-model.md)
- API Guide: [requirement-API_IMPLEMENTATION_GUIDE.md](contracts/requirement-API_IMPLEMENTATION_GUIDE.md)
- Visual design system: [design.md](../../design.md) (The Tactical Command)

## 1. File Structure

### Frontend

```
frontend/src/
├── features/
│   └── requirement/
│       ├── RequirementManagementView.vue     # page view — list/kanban/detail router-view host
│       ├── views/
│       │   ├── RequirementListView.vue       # requirement list with filtering/sorting
│       │   ├── RequirementKanbanView.vue     # kanban board view
│       │   └── RequirementDetailView.vue     # detail layout — hosts cards
│       ├── components/
│       │   ├── RequirementCard.vue           # reusable card wrapper (like IncidentCard)
│       │   ├── PriorityBadge.vue             # Critical/High/Medium/Low badge
│       │   ├── RequirementStatusBadge.vue    # status badge with LED indicator
│       │   ├── CategoryBadge.vue             # Functional/NFR/Security/etc badge
│       │   ├── RequirementListTable.vue      # requirement table rows
│       │   ├── StatusDistribution.vue        # status distribution summary strip
│       │   ├── RequirementHeaderCard.vue     # header with priority, status, assignee
│       │   ├── DescriptionCard.vue           # description + acceptance criteria
│       │   ├── LinkedStoriesCard.vue         # derived user stories list
│       │   ├── LinkedSpecsCard.vue           # linked specs with generation entry
│       │   ├── SdlcChainCard.vue             # SDLC chain traceability (reuse pattern)
│       │   ├── AiAnalysisCard.vue            # AI completeness, gaps, impact analysis
│       │   ├── PriorityMatrix.vue            # 2x2 impact vs effort matrix
│       │   ├── KanbanColumn.vue              # single kanban column
│       │   ├── ImportPanel.vue               # import modal/drawer host
│       │   ├── ImportDropZone.vue            # drag-and-drop file upload zone
│       │   ├── ImportTextInput.vue           # paste text textarea
│       │   ├── ImportSourceTabs.vue          # source type tabs (Text/File/Email/Meeting)
│       │   ├── NormalizationResultCard.vue   # AI draft review with editable fields
│       │   ├── MissingInfoBanner.vue         # amber warnings for missing fields
│       │   ├── BatchPreviewTable.vue         # Excel row preview with checkboxes
│       │   ├── ColumnMappingEditor.vue       # column-to-field mapping UI
│       │   └── BatchProgressBar.vue          # "3 of 12 normalized..." progress
│       ├── stores/
│       │   └── requirementStore.ts           # Pinia store — list + kanban + detail state
│       ├── api/
│       │   └── requirementApi.ts             # API client for /requirements/*
│       ├── types/
│       │   └── requirement.ts                # TypeScript interfaces
│       ├── profiles/
│       │   ├── index.ts                      # profile registry + resolver
│       │   ├── standardSddProfile.ts         # Standard SDD profile definition
│       │   └── ibmIProfile.ts               # IBM i profile definition (orchestrator-routed)
│       ├── components/
│       │   ├── ProfileBadge.vue              # shows active profile name in header
│       │   ├── ProfileChainCard.vue          # profile-adaptive SDLC chain card
│       │   ├── EntryPathSelector.vue         # read-only badge showing orchestrator-determined path
│       │   ├── SpecTierSelector.vue          # read-only badge showing orchestrator-determined tier
│       │   └── ProfileSkillActions.vue       # profile-specific action buttons (IBM i: single "Send to Orchestrator")
│       └── mockData.ts                       # Phase A mocked data
```

### Backend

```
backend/src/main/java/com/sdlctower/
├── domain/
│   └── requirement/
│       ├── RequirementController.java        # REST endpoints
│       ├── RequirementService.java           # domain logic + data assembly
│       └── dto/
│           ├── RequirementListDto.java
│           ├── RequirementListItemDto.java
│           ├── StatusDistributionDto.java
│           ├── RequirementDetailDto.java
│           ├── RequirementHeaderDto.java
│           ├── RequirementDescriptionDto.java
│           ├── AcceptanceCriterionDto.java
│           ├── LinkedStoryDto.java
│           ├── LinkedSpecDto.java
│           ├── AiAnalysisDto.java
│           └── SdlcChainLinkDto.java         # reuse from shared
├── shared/
│   ├── dto/
│   │   ├── ApiResponse.java                  # existing shared envelope
│   │   └── SectionResultDto.java             # shared section error isolation
│   └── ApiConstants.java                     # add requirement endpoint constants
├── src/main/resources/
│   └── db/migration/
│       └── V5__seed_requirement_data.sql     # seed data for requirements
├── src/test/java/com/sdlctower/domain/requirement/
│   └── RequirementControllerTest.java
```

---

## 2. Layout Composition

### 2.1 Requirement List View (default)

```
┌──────────────────────────────────────────────────────────────────┐
│  Context Bar (from shell)                                        │
├──────────────────────────────────────────────────────────────────┤
│  Page Header: "Requirement Management" + subtitle               │
│  [List] [Kanban] [Matrix]  ← view toggle (top-right)            │
├──────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────────┐│
│  │  Status Distribution Strip                                   ││
│  │  [Draft: 5] [In Review: 3] [Approved: 8] [In Progress: 4]   ││
│  │  [Delivered: 12] [Archived: 1]                                ││
│  ├──────────────────────────────────────────────────────────────┤│
│  │  Filter Bar: Priority | Status | Category | Assignee | Search ││
│  ├──────────────────────────────────────────────────────────────┤│
│  │  Active | Completed (tab toggle)                              ││
│  ├──────────────────────────────────────────────────────────────┤│
│  │  Requirement Table                                            ││
│  │  ID | Title | Priority | Status | Category | Stories | Specs  ││
│  │  ─────────────────────────────────────────────────────────── ││
│  │  REQ-0042 | Auth SSO Integration  | Critical | Approved | Fn ││
│  │  REQ-0041 | Rate Limiting Policy  | High     | Draft    | NFR││
│  │  REQ-0040 | Audit Log Export      | Medium   | In Prog  | Fn ││
│  └──────────────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 Requirement Kanban View

```
┌──────────────────────────────────────────────────────────────────┐
│  Context Bar (from shell)                                        │
├──────────────────────────────────────────────────────────────────┤
│  Page Header + [List] [Kanban*] [Matrix]                         │
├──────────────────────────────────────────────────────────────────┤
│  Filter Bar (shared with list view)                              │
├──────────────────────────────────────────────────────────────────┤
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐│
│  │  Draft   │ │ In Review│ │ Approved │ │In Progress│ │Delivered││
│  │  (5)     │ │  (3)     │ │  (8)     │ │  (4)      │ │  (12)  ││
│  │ ┌──────┐ │ │ ┌──────┐ │ │ ┌──────┐ │ │ ┌──────┐  │ │┌──────┐││
│  │ │REQ-41│ │ │ │REQ-38│ │ │ │REQ-42│ │ │ │REQ-40│  │ ││REQ-35│││
│  │ │Rate  │ │ │ │Cache │ │ │ │SSO   │ │ │ │Audit │  │ ││OAuth │││
│  │ │▪ High│ │ │ │▪ Med │ │ │ │▪ Crit│ │ │ │▪ Med │  │ ││▪ High│││
│  │ └──────┘ │ │ └──────┘ │ │ └──────┘ │ │ └──────┘  │ │└──────┘││
│  │ ┌──────┐ │ │          │ │ ┌──────┐ │ │           │ │        ││
│  │ │REQ-39│ │ │          │ │ │REQ-37│ │ │           │ │        ││
│  │ └──────┘ │ │          │ │ └──────┘ │ │           │ │        ││
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └────────┘│
└──────────────────────────────────────────────────────────────────┘
```

### 2.3 Priority Matrix View

```
┌──────────────────────────────────────────────────────────────────┐
│  Context Bar (from shell)                                        │
├──────────────────────────────────────────────────────────────────┤
│  Page Header + [List] [Kanban] [Matrix*]                         │
├──────────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────┬──────────────────────────────────┐│
│  │  HIGH IMPACT / LOW EFFORT  │  HIGH IMPACT / HIGH EFFORT      ││
│  │  "Quick Wins"              │  "Strategic"                     ││
│  │  ┌──────┐ ┌──────┐        │  ┌──────┐                       ││
│  │  │REQ-42│ │REQ-38│        │  │REQ-41│                       ││
│  │  └──────┘ └──────┘        │  └──────┘                       ││
│  ├────────────────────────────┼──────────────────────────────────┤│
│  │  LOW IMPACT / LOW EFFORT   │  LOW IMPACT / HIGH EFFORT       ││
│  │  "Fill-ins"                │  "Reconsider"                    ││
│  │  ┌──────┐                  │                                  ││
│  │  │REQ-40│                  │                                  ││
│  │  └──────┘                  │                                  ││
│  └────────────────────────────┴──────────────────────────────────┘│
└──────────────────────────────────────────────────────────────────┘
```

### 2.4 Requirement Detail View

```
┌──────────────────────────────────────────────────────────────────┐
│  Context Bar (from shell)                                        │
├──────────────────────────────────────────────────────────────────┤
│  ← Back to List | REQ-0042 | Page Actions                       │
├──────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────────┐│
│  │  Requirement Header Card (full width)                        ││
│  │  Title | Priority | Status | Category | Assignee             ││
│  │  Source: Manual | Created: 2026-04-10 | Updated: 2026-04-15 ││
│  └──────────────────────────────────────────────────────────────┘│
│                                                                  │
│  ┌────────────────────────────┐  ┌──────────────────────────────┐│
│  │  Description Card          │  │  Linked Stories Card         ││
│  │  (left column, spans 2     │  │  (right column)              ││
│  │   rows)                    │  ├──────────────────────────────┤│
│  │                            │  │  Linked Specs Card           ││
│  │  Full description          │  │  ┌─────────────────────────┐ ││
│  │  Business context          │  │  │ SPEC-0018 (Approved)    │ ││
│  │  Acceptance criteria       │  │  │ [GENERATE SPEC]         │ ││
│  │   ☑ Criterion 1            │  │  └─────────────────────────┘ ││
│  │   ☐ Criterion 2            │  │                              ││
│  └────────────────────────────┘  └──────────────────────────────┘│
│                                                                  │
│  ┌────────────────────────────┐  ┌──────────────────────────────┐│
│  │  AI Analysis Card          │  │  SDLC Chain Card             ││
│  │  Completeness | Gaps       │  │  Req → Story → Spec → ...   ││
│  │  Impact | Suggestions      │  │  [Expand full chain]         ││
│  └────────────────────────────┘  └──────────────────────────────┘│
└──────────────────────────────────────────────────────────────────┘
```

Detail view uses CSS grid:
- **Row 1**: Header card (full width)
- **Row 2-3**: Description card (left, spans 2 rows) + Linked Stories (right top) + Linked Specs (right bottom)
- **Row 4**: AI Analysis (left) + SDLC Chain (right)

---

## 3. Component API Contracts

### 3.1 RequirementListTable

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `requirements` | `readonly RequirementListItem[]` | Yes | Filtered requirement list |
| `sortField` | `SortField` | Yes | Current sort column |
| `sortDirection` | `'asc' \| 'desc'` | Yes | Sort direction |

| Event | Payload | Description |
|-------|---------|-------------|
| `@select` | `requirementId: string` | User clicks a row |
| `@sort` | `{ field: SortField, order: 'asc' \| 'desc' }` | User changes sort |

### 3.2 PriorityBadge

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `priority` | `RequirementPriority` | Yes | Critical, High, Medium, or Low |

Visual mapping:
- Critical: `--color-error` (crimson `#ffb4ab`) with tinted background `rgba(255, 180, 171, 0.1)`
- High: amber `#F59E0B` with tinted background `rgba(245, 158, 11, 0.1)`
- Medium: `--color-secondary` (cyan `#89ceff`) with tinted background `rgba(137, 206, 255, 0.1)`
- Low: `--color-on-surface-variant` with muted background

### 3.3 RequirementStatusBadge

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `status` | `RequirementStatus` | Yes | Current requirement status |

Visual: 6px LED pip + status text. LED color mapping:
- Draft: `--color-outline-variant` (muted, no pulse)
- In Review: amber `#F59E0B` (subtle pulse)
- Approved: `--color-tertiary` (emerald `#4edea3`, no pulse)
- In Progress: `--color-secondary` (cyan `#89ceff`, subtle pulse)
- Delivered: `--color-tertiary` (emerald, no pulse)
- Archived: `--color-on-surface-variant` (muted, no pulse)

### 3.4 CategoryBadge

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `category` | `RequirementCategory` | Yes | Functional, Non-Functional, Technical, Business |

Visual: Pill badge with subtle tinted background per category:
- Functional: `--color-secondary` at 10% opacity, cyan text
- Non-Functional: `--color-tertiary` at 10% opacity, emerald text
- Technical: `--color-primary` at 10% opacity, primary text
- Business: amber at 10% opacity, amber text
- Security: `--color-error` at 10% opacity, crimson text

### 3.5 StatusDistribution

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `distribution` | `StatusDistribution` | Yes | Count per status |

| Event | Payload | Description |
|-------|---------|-------------|
| `@filter` | `status: RequirementStatus` | User clicks a status segment to filter |

Visual: Horizontal strip of status segments similar to SeverityDistribution in incident. Each segment shows status name, LED pip, and count. Clicking a segment applies it as a filter.

### 3.6 RequirementHeaderCard

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `header` | `SectionResult<RequirementHeader>` | Yes | Header data with error isolation |

Renders: ID (JetBrains Mono), title, priority badge, status badge, category badge, assignee, source indicator (Manual/Imported/AI-Generated), created and updated timestamps, story count, spec count and coverage indicator.

### 3.7 DescriptionCard

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `description` | `SectionResult<RequirementDescription>` | Yes | Description + acceptance criteria |

Renders: Full requirement description text, business context and rationale section, business value justification, acceptance criteria list with pass/fail checkmarks. Criteria use `--color-tertiary` for passed and `--color-on-surface-variant` for not yet evaluated.

### 3.8 LinkedStoriesCard

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `stories` | `SectionResult<LinkedStories>` | Yes | Derived user stories |

| Event | Payload | Description |
|-------|---------|-------------|
| `@navigate` | `storyId: string` | User clicks a story row |
| `@generate` | `void` | User clicks "Generate Stories" |

Renders: List of derived user stories showing story ID (JetBrains Mono), title, status badge, linked spec indicator. Decomposition completeness bar at the top. "Generate Stories" button uses the secondary/cyan AI styling with outer glow.

### 3.9 LinkedSpecsCard

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `specs` | `SectionResult<LinkedSpecs>` | Yes | Linked spec objects |

| Event | Payload | Description |
|-------|---------|-------------|
| `@navigate` | `specId: string` | User clicks a spec row |
| `@generate` | `{ sourceType: 'requirement' \| 'story', sourceId: string }` | User clicks "Generate Spec" |

Renders: List of linked specs showing spec ID (JetBrains Mono), title, version, status badge. Spec coverage indicator. "Generate Spec" button is prominent (AI-styled, secondary color with glow) per REQ-REQ-40 — Spec generation is a primary workflow, never buried.

### 3.10 SdlcChainCard

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `chain` | `SectionResult<SdlcChain>` | Yes | Chain links |

| Event | Payload | Description |
|-------|---------|-------------|
| `@navigate` | `routePath: string` | User clicks a chain link |

Renders: Compressed SDLC chain with Spec node always visible per REQ-REQ-52. Expand control for collapsed nodes. Chain nodes: Requirement (current, highlighted) -> User Story -> Spec (always visible) -> Architecture -> Design -> Tasks -> Code -> Test -> Deploy. Chain completeness health indicator (green/yellow/red LED) per REQ-REQ-53.

### 3.11 AiAnalysisCard

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `analysis` | `SectionResult<AiAnalysis>` | Yes | AI analysis results |

| Event | Payload | Description |
|-------|---------|-------------|
| `@runAnalysis` | `void` | User requests AI analysis |

Renders: Completeness score (circular progress), quality assessment (clarity, testability, ambiguity scores), gap analysis results, impact assessment summary, improvement suggestions list. If no analysis has been run, shows "Run AI Analysis" call-to-action button (AI-styled). Scores use green/amber/red color mapping.

### 3.12 PriorityMatrix

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `requirements` | `readonly RequirementListItem[]` | Yes | Requirements with impact/effort scores |

| Event | Payload | Description |
|-------|---------|-------------|
| `@select` | `requirementId: string` | User clicks a requirement dot |

Renders: 2x2 grid with quadrant labels (Quick Wins, Strategic, Fill-ins, Reconsider). Requirement dots positioned by impact (y-axis) and effort (x-axis). Dots colored by priority. Quadrant backgrounds use subtle tints on `--color-surface-container-low`.

### 3.13 KanbanColumn

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `status` | `RequirementStatus` | Yes | Column status |
| `requirements` | `readonly RequirementListItem[]` | Yes | Requirements in this column |
| `count` | `number` | Yes | Total count (may differ from array if paginated) |

| Event | Payload | Description |
|-------|---------|-------------|
| `@select` | `requirementId: string` | User clicks a card |
| `@drop` | `{ requirementId: string, newStatus: RequirementStatus }` | User drops a card |

Renders: Column header with status name, LED pip, and count. Vertical stack of requirement mini-cards. Each mini-card shows ID, title (truncated), priority badge. Column background uses `--color-surface-container-low`. Cards use `--color-surface-container-high`.

### 3.14 RequirementCard

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `title` | `string` | Yes | Card title text |
| `loading` | `boolean` | No | Show skeleton state |
| `error` | `string \| null` | No | Error message to display |

| Slot | Description |
|------|-------------|
| `default` | Card body content |
| `actions` | Optional card header actions |

| Event | Payload | Description |
|-------|---------|-------------|
| `@retry` | `void` | User clicks retry on error state |

Renders: Card wrapper following design.md section 5 — `surface-container-high` background, 4px radius, no borders. Three states: loading (skeleton), error (message + retry), content (default slot).

### Profile-Adaptive Components

#### ProfileBadge.vue
| Prop | Type | Description |
|------|------|-------------|
| `profile` | `PipelineProfile` | Active profile object |

Renders a subtle badge in the page header showing the active profile name (e.g., "Standard SDD", "IBM i"). Uses `label-md` typography in uppercase with HUD styling.

#### ProfileChainCard.vue
| Prop | Type | Description |
|------|------|-------------|
| `chainNodes` | `ChainNode[]` | From `activeProfile.chainNodes` |
| `currentNodeStatuses` | `Record<string, ChainNodeStatus>` | Status per node |
| `executionHubNodeId` | `string` | Node to highlight as execution hub |

Replaces the hardcoded SdlcChainCard for requirement detail view. Renders chain nodes dynamically from the profile. The execution hub node gets the cyan highlight + glow treatment. Nodes use the same LED pip pattern as other status indicators.

#### EntryPathSelector.vue
| Prop | Type | Description |
|------|------|-------------|
| `paths` | `EntryPath[]` | From `activeProfile.entryPaths` |
| `determinedPathId` | `string \| null` | Orchestrator-determined path (read-only) |

Shows only when the active profile has >1 entry path AND the orchestrator has returned a result. Renders as a read-only badge showing the orchestrator's determined path label and description. This is not an interactive selector — the path is determined by `ibm-i-workflow-orchestrator`, not by the user. Before the orchestrator has been invoked, this area shows a placeholder: "Path will be determined by orchestrator".

#### SpecTierSelector.vue
| Prop | Type | Description |
|------|------|-------------|
| `tiers` | `SpecTier[]` | From `activeProfile.specTiering.tiers` |
| `determinedTierId` | `string \| null` | Orchestrator-determined tier (read-only) |

Shows only when `activeProfile.specTiering` is not null AND the orchestrator has returned a result. Renders as a read-only badge showing the orchestrator-determined tier (e.g., "L2 Standard") with a description tooltip. This is not an interactive selector — the tier is determined by `ibm-i-workflow-orchestrator`, not by the user. Before the orchestrator has been invoked, this area shows a placeholder: "Tier will be determined by orchestrator".

#### ProfileSkillActions.vue
| Prop | Type | Description |
|------|------|-------------|
| `skills` | `SkillBinding[]` | From `activeProfile.skills` |
| `requirementId` | `string` | Current requirement ID |
| `usesOrchestrator` | `boolean` | From `activeProfile.usesOrchestrator` |

| Event | Payload | Description |
|-------|---------|-------------|
| `invoke` | `{ skillId: string }` | Emitted when user clicks a skill action |

For Standard SDD: renders action buttons from the profile's skill bindings (e.g., "Generate Stories", "Generate Spec"). Button labels come from `SkillBinding.label`.

For IBM i (when `usesOrchestrator` is true): renders a single "Send to Orchestrator" button that invokes `ibm-i-workflow-orchestrator`. The orchestrator determines path and tier internally. No entry path selector or tier selector is shown as user input.

### Intake Components

#### ImportPanel.vue
| Prop | Type | Description |
|------|------|-------------|
| open | boolean | Whether the panel is visible |
| profileId | string | Active pipeline profile ID |
| Event | Payload | |
| close | void | Emitted when panel should close |
| created | RequirementListItem | Emitted when a draft is confirmed |

Full-screen modal or right drawer (840px width). Hosts the import flow state machine: input → parsing → normalizing → review (or batch-preview → batch-normalizing → batch-review). Background uses `surface-container-low` with glass overlay on the main page.

#### ImportDropZone.vue
| Prop | Type | Description |
|------|------|-------------|
| acceptedFormats | string[] | ['.xlsx', '.csv', '.pdf', '.eml', '.msg', '.vtt', '.txt', '.png', '.jpg', '.jpeg', '.webp'] |
| maxSizeMb | number | 10 |
| Event | Payload | |
| file-selected | File | Browser File object |

Dashed border zone (ghost border style, `outline_variant` at 20% opacity). On drag-over: border becomes `secondary` (cyan) with subtle glow. Shows accepted format icons and "Drop file here or click to browse" text. File size validation with error message.

#### ImportSourceTabs.vue
| Prop | Type | Description |
|------|------|-------------|
| activeTab | InputSourceType | Currently selected source type |
| Event | Payload | |
| update:activeTab | InputSourceType | Tab changed |

Horizontal tab bar: [Paste Text] [Upload File] [Email] [Meeting Summary]. Uses `label-md` uppercase, selected tab has `secondary` bottom border accent.

#### NormalizationResultCard.vue
| Prop | Type | Description |
|------|------|-------------|
| draft | RequirementDraft | AI-produced draft |
| editable | boolean | Whether fields are editable |
| Event | Payload | |
| update:draft | RequirementDraft | Updated draft after user edits |
| confirm | void | User confirms draft |
| discard | void | User discards draft |

Shows all draft fields in a structured form layout. AI-suggested values have a small cyan "AI" badge. Missing info flags appear as amber banners at the top. Open questions appear in a collapsible section. Three action buttons at bottom: "Edit Draft" (toggles editable), "Confirm & Create" (primary, cyan gradient), "Discard" (tertiary).

#### MissingInfoBanner.vue
| Prop | Type | Description |
|------|------|-------------|
| flags | MissingInfoFlag[] | Missing info from normalization |

Amber background strip (`#F59E0B` at 10% opacity) with warning icon and text. Each flag is a separate line. Severity 'warning' gets amber icon, 'info' gets muted icon.

#### BatchPreviewTable.vue
| Prop | Type | Description |
|------|------|-------------|
| rows | ExcelRow[] | Parsed Excel rows |
| columnMappings | ColumnMapping[] | Current column-to-field mappings |
| Event | Payload | |
| toggle-row | number (rowIndex) | Row selection toggled |
| toggle-all | boolean | Select/deselect all |
| update:columnMappings | ColumnMapping[] | Column mapping changed |

Table with checkbox column, row index, and mapped field columns. Auto-detected mappings shown with green check, manual overrides with edit icon. Header row has "Select All" checkbox. Uses `surface-container-high` for table body, `surface-container-low` for header.

#### BatchProgressBar.vue
| Prop | Type | Description |
|------|------|-------------|
| progress | BatchImportProgress | Current progress |

Horizontal progress bar with `secondary` (cyan) fill. Text shows "3 of 12 normalized..." in `body-sm`. Errors shown as crimson count badge.

---

## 4. Visual Design Decisions

### 4.1 Color Usage

| Element | Token / Value | Usage |
|---------|---------------|-------|
| Critical priority | `--color-error` (`#ffb4ab`) | Priority badge, LED, row tint |
| Critical tint | `rgba(255, 180, 171, 0.1)` | Row/card background for Critical items |
| High priority | `#F59E0B` | Priority badge, LED |
| Medium priority | `--color-secondary` (`#89ceff`) | Priority badge, LED |
| Low priority | `--color-on-surface-variant` | Priority badge, LED |
| Draft status | `--color-outline-variant` (`#45464d`) | LED, badge outline style |
| In Review status | `#F59E0B` | LED (amber pulse), badge |
| Approved status | `--color-tertiary` (`#4edea3`) | LED (emerald), badge |
| In Progress status | `--color-secondary` (`#89ceff`) | LED (cyan pulse), badge |
| Delivered status | `--color-tertiary` (`#4edea3`) | LED, badge |
| Archived status | `--color-on-surface-variant` | LED (muted), badge |
| AI actions / generate buttons | `--color-secondary` (`#89ceff`) | Button background with 8px outer glow |
| Spec node (chain) | `--color-secondary` | Always highlighted in SDLC chain |
| Chain health green | `--color-tertiary` | Complete chain |
| Chain health amber | `#F59E0B` | Partial chain |
| Chain health red | `--color-error` | Broken chain |

### 4.2 Typography

| Element | Font | Size | Style |
|---------|------|------|-------|
| Requirement ID | JetBrains Mono | `text-tech` (0.75rem) | Uppercase |
| Spec / Story IDs | JetBrains Mono | 0.75rem | Uppercase |
| Timestamps | JetBrains Mono | 0.6875rem | Muted color |
| Card headers | Inter | `text-label` | Uppercase, letter-spacing 0.05em |
| Body text | Inter | `body-sm` (0.75rem) | Standard |
| Page header | Inter | `display-sm` | High-contrast `--color-on-surface` |
| Acceptance criteria | Inter | `body-sm` | Checklist style with status indicator |
| AI analysis scores | JetBrains Mono | 0.875rem | Numeric, score-colored |
| Kanban card title | Inter | `body-sm` | Truncated with ellipsis |
| Matrix quadrant labels | Inter | `label-md` | Uppercase, 0.05em spacing |

### 4.3 Card Style

All cards follow `design.md` section 5:
- Background: `--color-surface-container-high` (`#222a3d`)
- Radius: `4px` (strict `--radius-sm`)
- No borders (the "No-Line" rule)
- Separation via background shifts and 16px whitespace
- Card dividers strictly forbidden — use 16px vertical whitespace
- Error state: card body replaced with error message + retry button

### 4.4 Status LED Indicators

Status LEDs follow the existing pattern:
- `led-emerald` — Approved, Delivered (healthy states)
- `led-cyan` — In Progress (active processing)
- `led-amber` — In Review (pending states, subtle pulse)
- `led-muted` — Draft, Archived (inactive states)

Active states (In Review, In Progress) use a subtle CSS pulse animation matching the incident pattern.

### 4.5 Kanban Visual Rules

- Column background: `--color-surface-container-low` (`#131b2e`)
- Card background: `--color-surface-container-high` (`#222a3d`)
- Card hover: `--color-surface-container-highest` (`#2d3449`)
- Drop target highlight: 2px dashed `--color-secondary` at 40% opacity
- Column header: Inter `label-md`, uppercase, with status LED and count badge
- Card spacing: 8px gap between cards within a column
- Column spacing: 12px gap between columns
- Column min-width: 220px

### 4.6 Priority Matrix Visual Rules

- Grid background: `--color-surface-container-low`
- Quadrant dividers: `--color-outline-variant` at 20% opacity (Ghost Border)
- Quick Wins quadrant: subtle `--color-tertiary` at 5% tint
- Strategic quadrant: subtle `--color-secondary` at 5% tint
- Fill-ins quadrant: no tint (neutral)
- Reconsider quadrant: subtle `--color-error` at 5% tint
- Requirement dots: 12px circles, colored by priority, hover shows tooltip
- Axis labels: Inter `label-sm`, muted color

#### Profile Badge Styling
- Background: `surface-container-high`
- Text: `on-surface` at 70% opacity
- Typography: `label-md` uppercase, 0.05em letter-spacing
- Icon: small pipeline icon before text

#### Import Panel Styling
- Overlay: `surface_variant` at 60% opacity with 20px backdrop blur (glassmorphism per design system)
- Panel background: `surface-container-low`
- Drop zone: ghost border (`outline_variant` at 20%), dashed, 4px radius
- Drop zone active: `secondary` border, subtle outer glow
- Source tabs: HUD-style label-md uppercase
- "Normalize with AI" button: AI gradient (secondary → on_secondary_container at 135°)
- "Confirm & Create" button: same AI gradient
- "Discard" button: tertiary style (transparent, outline text)
- AI badge on suggested values: small cyan pill with "AI" text in label-sm
- Missing info banners: amber (#F59E0B) at 10% opacity background

#### Entry Path Display Styling (read-only badge)
- Read-only badge with `surface-container-low` background
- Determined path: `secondary` (cyan) border-left accent with path label
- Path description: `body-sm` in `on-surface-variant`
- Before orchestrator invocation: muted placeholder text "Path will be determined by orchestrator"

#### Spec Tier Display Styling (read-only badge)
- Read-only badge showing determined tier
- Determined: `secondary` background, `on-secondary` text
- Undetermined: `surface-container-high` background with muted placeholder
- Tier description: tooltip on hover

---

## 5. State Management

### 5.1 Store Shape

```
requirementStore
├── list
│   ├── requirements: RequirementListItem[]
│   ├── statusDistribution: StatusDistribution
│   ├── isLoading: boolean
│   ├── error: string | null
│   └── filters: RequirementFilters
├── kanban
│   ├── columns: Map<RequirementStatus, RequirementListItem[]>
│   ├── isLoading: boolean
│   └── error: string | null
├── detail
│   ├── header: SectionResult<RequirementHeader>
│   ├── description: SectionResult<RequirementDescription>
│   ├── linkedStories: SectionResult<LinkedStories>
│   ├── linkedSpecs: SectionResult<LinkedSpecs>
│   ├── sdlcChain: SectionResult<SdlcChain>
│   ├── aiAnalysis: SectionResult<AiAnalysis>
│   ├── isLoading: boolean
│   └── error: string | null
├── activeView: 'list' | 'kanban' | 'matrix'
└── selectedRequirementId: string | null
```

### 5.2 Store Actions

| Action | Triggers | Effect |
|--------|----------|--------|
| `fetchRequirementList()` | List view mount, filter change | Fetches list from API or mock |
| `fetchRequirementDetail(id)` | Detail view mount | Fetches detail from API or mock |
| `generateStories(requirementId)` | User clicks Generate Stories | POST generate, refresh linked stories |
| `generateSpec(sourceType, sourceId)` | User clicks Generate Spec | POST generate, refresh linked specs |
| `runAiAnalysis(requirementId)` | User clicks Run AI Analysis | POST analysis, refresh AI analysis card |
| `moveRequirement(id, newStatus)` | Kanban drag-and-drop | PATCH status, refresh kanban columns |
| `setFilters(filters)` | User changes filters | Updates filter state, re-fetches list |
| `setActiveView(view)` | User toggles view mode | Updates activeView |
| `clearDetail()` | Navigate back to list | Resets detail state |

### 5.3 Phase A / Phase B Toggle

The store checks `import.meta.env.DEV && !import.meta.env.VITE_USE_BACKEND`:
- When `true` (dev without backend): imports from `mockData.ts` directly
- When `false` (backend available): calls `requirementApi.ts` functions

This matches the verified pattern in `dashboardStore.ts:7`.

---

## 6. Routing

### 6.1 Route Configuration

The requirement page uses nested routes within the existing shell:

| Route | Component | Purpose |
|-------|-----------|---------|
| `/requirements` | `RequirementListView.vue` | Default list view (or kanban/matrix via view toggle) |
| `/requirements/:requirementId` | `RequirementDetailView.vue` | Detail view for specific requirement |

`RequirementManagementView.vue` becomes a `<router-view>` host for these child routes.

### 6.2 Navigation

- Shell nav -> `/requirements` -> List view (default)
- View toggle -> switches between list, kanban, and matrix (same route, state-driven)
- List row click / Kanban card click -> `router.push({ name: 'requirement-detail', params: { requirementId } })`
- Detail back button -> `router.push({ name: 'requirements' })`
- SDLC chain link -> `router.push(routePath)`
- Linked story click -> `router.push({ name: 'story-detail', params: { storyId } })` (future)
- Linked spec click -> `router.push({ name: 'spec-detail', params: { specId } })` (future)

---

## 7. API Contracts (Summary)

Full contracts are defined in
[requirement-API_IMPLEMENTATION_GUIDE.md](contracts/requirement-API_IMPLEMENTATION_GUIDE.md).

### 7.1 Endpoints

| Method | Path | Purpose | Response |
|--------|------|---------|----------|
| GET | `/api/v1/requirements` | List with filters | `ApiResponse<RequirementListDto>` |
| GET | `/api/v1/requirements/:id` | Full detail | `ApiResponse<RequirementDetailDto>` |
| GET | `/api/v1/requirements/:id/chain` | SDLC chain | `ApiResponse<SdlcChainDto>` |
| GET | `/api/v1/requirements/:id/analysis` | AI analysis | `ApiResponse<AiAnalysisDto>` |
| POST | `/api/v1/requirements/:id/generate-stories` | Generate user stories | `ApiResponse<LinkedStoriesDto>` |
| POST | `/api/v1/requirements/:id/generate-spec` | Generate spec | `ApiResponse<LinkedSpecDto>` |
| PATCH | `/api/v1/requirements/:id/status` | Update status (kanban) | `ApiResponse<RequirementHeaderDto>` |

### 7.2 Query Parameters for List

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `priority` | `string` | — | Filter by priority |
| `status` | `string` | — | Filter by status |
| `category` | `string` | — | Filter by category |
| `assignee` | `string` | — | Filter by assignee |
| `search` | `string` | — | Text search across title and description |
| `showCompleted` | `boolean` | `false` | Include Delivered/Archived requirements |
| `sortBy` | `string` | `updatedAt` | Sort field |
| `sortDirection` | `string` | `desc` | Sort direction |

---

## 8. Data Model (Key Frontend Interfaces)

Full data model is defined in
[requirement-data-model.md](../04-architecture/requirement-data-model.md).

### 8.1 Enums / Union Types

```typescript
export type RequirementPriority = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export type RequirementStatus =
  | 'DRAFT'
  | 'IN_REVIEW'
  | 'APPROVED'
  | 'IN_PROGRESS'
  | 'DELIVERED'
  | 'ARCHIVED';

export type RequirementCategory =
  | 'FUNCTIONAL'
  | 'NON_FUNCTIONAL'
  | 'TECHNICAL'
  | 'BUSINESS';

export type RequirementSource = 'MANUAL' | 'IMPORTED' | 'AI_GENERATED';

export type SpecStatus = 'DRAFT' | 'IN_REVIEW' | 'APPROVED' | 'IMPLEMENTED' | 'VERIFIED' | 'SUPERSEDED';

export type SortField = 'priority' | 'status' | 'category' | 'updatedAt' | 'title';

export type SdlcArtifactType =
  | 'requirement'
  | 'user_story'
  | 'spec'
  | 'architecture'
  | 'design'
  | 'tasks'
  | 'code'
  | 'test'
  | 'deploy';
```

### 8.2 List Types

```typescript
export interface StatusDistribution {
  readonly DRAFT: number;
  readonly IN_REVIEW: number;
  readonly APPROVED: number;
  readonly IN_PROGRESS: number;
  readonly DELIVERED: number;
  readonly ARCHIVED: number;
}

export interface RequirementListItem {
  readonly id: string;
  readonly title: string;
  readonly priority: RequirementPriority;
  readonly status: RequirementStatus;
  readonly category: RequirementCategory;
  readonly assignee: string;
  readonly storyCount: number;
  readonly specCount: number;
  readonly specCoverage: number;     // 0.0 to 1.0
  readonly impact: number;          // 1-10 for matrix
  readonly effort: number;          // 1-10 for matrix
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface RequirementFilters {
  readonly priority?: RequirementPriority;
  readonly status?: RequirementStatus;
  readonly category?: RequirementCategory;
  readonly assignee?: string;
  readonly search?: string;
  readonly showCompleted: boolean;
}

export interface RequirementList {
  readonly statusDistribution: StatusDistribution;
  readonly requirements: ReadonlyArray<RequirementListItem>;
}
```

### 8.3 Detail Types

```typescript
export interface RequirementHeader {
  readonly id: string;
  readonly title: string;
  readonly priority: RequirementPriority;
  readonly status: RequirementStatus;
  readonly category: RequirementCategory;
  readonly assignee: string;
  readonly source: RequirementSource;
  readonly storyCount: number;
  readonly specCount: number;
  readonly specCoverage: number;
  readonly chainHealth: 'green' | 'amber' | 'red';
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface AcceptanceCriterion {
  readonly id: string;
  readonly description: string;
  readonly passed: boolean | null;  // null = not yet evaluated
}

export interface RequirementDescription {
  readonly description: string;
  readonly businessContext: string;
  readonly businessValue: string;
  readonly stakeholders: ReadonlyArray<string>;
  readonly externalRefs: ReadonlyArray<{ readonly label: string; readonly url: string }>;
  readonly acceptanceCriteria: ReadonlyArray<AcceptanceCriterion>;
}

export interface LinkedStory {
  readonly id: string;
  readonly title: string;
  readonly status: string;
  readonly hasSpec: boolean;
  readonly routePath: string;
}

export interface LinkedStories {
  readonly stories: ReadonlyArray<LinkedStory>;
  readonly decompositionCompleteness: number;  // 0.0 to 1.0
}

export interface LinkedSpec {
  readonly id: string;
  readonly title: string;
  readonly version: string;
  readonly status: SpecStatus;
  readonly routePath: string;
}

export interface LinkedSpecs {
  readonly specs: ReadonlyArray<LinkedSpec>;
  readonly coverage: number;  // 0.0 to 1.0
}

export interface QualityBreakdown {
  readonly completeness: number;   // 0-100
  readonly clarity: number;        // 0-100
  readonly testability: number;    // 0-100
  readonly consistency: number;    // 0-100
}

export interface AiSuggestion {
  readonly type: 'AMBIGUITY' | 'GAP' | 'INCONSISTENCY' | 'IMPROVEMENT';
  readonly severity: 'HIGH' | 'MEDIUM' | 'LOW';
  readonly message: string;
  readonly field: string;
}

export interface DuplicateCheck {
  readonly hasPotentialDuplicates: boolean;
  readonly candidates: ReadonlyArray<{ readonly id: string; readonly title: string; readonly similarity: number }>;
}

export interface ImpactAnalysis {
  readonly downstreamArtifacts: number;
  readonly affectedTeams: ReadonlyArray<string>;
  readonly riskLevel: 'HIGH' | 'MEDIUM' | 'LOW';
}

export interface AiAnalysis {
  readonly qualityScore: number;   // 0-100
  readonly qualityBreakdown: QualityBreakdown;
  readonly suggestions: ReadonlyArray<AiSuggestion>;
  readonly duplicateCheck: DuplicateCheck;
  readonly impactAnalysis: ImpactAnalysis;
  readonly analyzedAt: string;
}

export interface SdlcChainLink {
  readonly artifactType: SdlcArtifactType;
  readonly artifactId: string;
  readonly artifactTitle: string;
  readonly routePath: string;
  readonly exists: boolean;
}

export interface SdlcChain {
  readonly links: ReadonlyArray<SdlcChainLink>;
  readonly completeness: 'green' | 'amber' | 'red';
}
```

### 8.4 Detail Aggregate

```typescript
export interface RequirementDetail {
  readonly header: SectionResult<RequirementHeader>;
  readonly description: SectionResult<RequirementDescription>;
  readonly stories: SectionResult<LinkedStories>;
  readonly specs: SectionResult<LinkedSpecs>;
  readonly chain: SectionResult<SdlcChain>;
  readonly analysis: SectionResult<AiAnalysis>;
}
```

---

## 9. Database Schema (Summary)

Full DDL is defined in [requirement-data-model.md](../04-architecture/requirement-data-model.md).

### 9.1 Core Tables

| Table | Purpose |
|-------|---------|
| `requirement` | Core requirement entity |
| `acceptance_criterion` | Structured acceptance criteria per requirement |
| `requirement_story_link` | Parent-child relationship: requirement -> user stories |
| `requirement_spec_link` | Requirement -> spec linkage |
| `requirement_chain` | SDLC chain traceability records |
| `requirement_ai_analysis` | Stored AI analysis results |
| `requirement_audit_log` | Change tracking and governance audit trail |

### 9.2 Seed Data Migration

`V5__seed_requirement_data.sql` provides sample requirements for Phase A/B development:
- 8-10 requirements across different priorities, statuses, and categories
- Linked user stories and specs for chain demonstration
- Pre-computed AI analysis results for representative requirements
- Audit log entries for governance trail demonstration

---

## 10. Error and State Handling

### 10.1 List View States

| State | Behavior |
|-------|----------|
| Loading | Skeleton rows in table area, skeleton segments in distribution strip |
| Error | Error card with message + retry button |
| Empty (no requirements) | "No requirements yet — create your first requirement to start the SDD chain" with guidance icon |
| Empty (filtered) | "No requirements match the current filters" with clear filters action |
| Loaded | Full requirement table with distribution strip |

### 10.2 Kanban View States

| State | Behavior |
|-------|----------|
| Loading | Skeleton columns with placeholder cards |
| Error | Error card replacing kanban board |
| Empty | Empty columns with "No requirements" placeholder |
| Loaded | Full kanban board with cards in columns |

### 10.3 Detail View States

| State | Behavior |
|-------|----------|
| Loading | All cards show skeleton placeholders |
| Error (full) | Error card replacing entire detail view |
| Partial error | Individual cards show inline error with retry |
| Loaded | All cards render their data |

### 10.4 Per-Card Error Isolation

Each card receives `SectionResult<T>`. Component template:

```
if section.error -> render error message + retry button
else if section.data -> render card content
else -> render loading skeleton
```

### 10.5 AI Action States

| State | Behavior |
|-------|----------|
| No analysis run | "Run AI Analysis" call-to-action (AI-styled button) |
| Analysis running | Loading spinner with "Analyzing requirement..." text |
| Analysis complete | Full analysis card with scores and suggestions |
| Analysis failed | Error message with retry |
| Story generation in progress | Loading state on LinkedStoriesCard |
| Spec generation in progress | Loading state on LinkedSpecsCard |

### 10.6 Import Flow States

| State | UI |
|-------|-----|
| idle | Import panel closed |
| input | Panel open, awaiting text/file input |
| parsing | File parsing spinner (brief, client-side) |
| normalizing | Full-card skeleton loading with "AI is analyzing..." message |
| review | Draft form with all fields populated |
| batch-preview | Excel preview table with checkboxes |
| batch-normalizing | Progress bar with row-by-row updates |
| batch-review | Scrollable list of draft cards |
| error | Error banner with retry button |

---

## 11. Validation and Error Handling

### 11.1 Frontend Validation

| Action | Validation | Error Handling |
|--------|-----------|----------------|
| Filter change | Validate date range (from <= to) | Show inline validation message |
| URL param | Validate `:requirementId` matches `REQ-NNNN` | Redirect to list if invalid |
| Kanban drop | Validate status transition against state machine | Show toast with "Invalid status transition" |
| View toggle | Validate view name is valid enum | Default to list view |

### 11.2 Backend Validation

| Endpoint | Validation | Error Response |
|----------|-----------|----------------|
| `GET /requirements/:id` | ID exists in workspace | 404 with "Requirement not found" |
| `POST /generate-stories` | Requirement exists, status is Approved or later | 400 with "Requirement must be Approved before generating stories" |
| `POST /generate-spec` | Requirement or story exists | 400 with validation message |
| `PATCH /status` | Valid status transition per state machine | 400 with "Invalid status transition from X to Y" |

---

## 12. Integration Boundary

### 12.1 Upstream Dependencies

| System | Integration |
|--------|-------------|
| Shared App Shell | Provides navigation rail, context bar, AI command panel |
| Dashboard | Links to `/requirements` from requirement health card |
| Incident Module | SDLC chain links back to originating requirement (REQ-REQ-51) |

### 12.2 Downstream Dependencies

| System | Integration |
|--------|-------------|
| User Story Management | Linked stories navigate to story detail (future) |
| Spec Management | Linked specs navigate to spec detail (future) |
| AI Center | Skill execution for `req-to-user-story` and `user-story-to-spec` (future) |

### 12.3 Shared Components

| Component | Source | Usage |
|-----------|--------|-------|
| `SectionResult<T>` | `@/shared/types/section.ts` | Per-card error isolation envelope |
| `SdlcChainLink` | Reuse from shared | Chain traceability across modules |
| `ApiResponse<T>` | `shared/dto/ApiResponse.java` | Backend response envelope |
| `SectionResultDto<T>` | `shared/dto/SectionResultDto.java` | Backend section error isolation |

### 12.4 Pipeline Profile Integration

| System | Integration |
|--------|-------------|
| **Pipeline Profile Registry** | V1 uses hardcoded profiles in `profiles/index.ts`. V2 will load from Platform Center API (`GET /api/v1/pipeline-profiles/active`) |
| **build-agent-skill (IBM i)** | IBM i skills are external. The Requirement Management page triggers them via the skill invocation API but does not execute them directly. |

---

## 13. Testing Considerations

### 13.1 Backend Tests

| Test | Assertions |
|------|-----------|
| GET /requirements returns 200 | Response contains requirements array and status distribution |
| GET /requirements with filters | Filtered results match criteria |
| GET /requirements/:id returns 200 | Response contains all 6 sections |
| GET /requirements/:id with invalid ID returns 404 | Error response |
| GET /requirements/:id/chain returns 200 | Chain links are present |
| POST /generate-stories returns 200 | Stories created and linked |
| POST /generate-spec returns 200 | Spec created and linked |
| PATCH /status with valid transition returns 200 | Status updated |
| PATCH /status with invalid transition returns 400 | Error with message |

### 13.2 Frontend Tests (future)

| Test | Assertions |
|------|-----------|
| List renders requirements from store | Table rows match requirement count |
| Priority badges use correct colors | Critical uses crimson |
| Status distribution shows all statuses | All segments rendered |
| View toggle switches between list/kanban/matrix | Correct view component rendered |
| Detail view loads all 6 cards | All cards render from mock data |
| Empty state shows guidance message | "No requirements yet" text visible |
| Kanban drag triggers status update | Store action invoked with new status |
| Generate Stories button calls store action | Store method invoked |
| Generate Spec button calls store action | Store method invoked |

---

## 14. Risks / Design Tradeoffs

| # | Tradeoff | Decision |
|---|----------|----------|
| 1 | Nested routes vs. single view with conditional rendering | Nested routes — cleaner URL support, code splitting, browser back works naturally |
| 2 | Three view modes (list/kanban/matrix) on same route vs. separate routes | Same route with state toggle — avoids URL confusion, shared filters |
| 3 | Description + Acceptance Criteria in one card vs. two | One card — they are conceptually linked (description defines what, criteria defines done) |
| 4 | AI Analysis as separate card vs. inline in header | Separate card — analysis can be heavy and should not block header rendering |
| 5 | Kanban drag-and-drop vs. status dropdown | Drag-and-drop with state machine validation — more intuitive but requires transition guard |
| 6 | Priority matrix as view mode vs. separate card in detail | View mode in list — matrix is a list-level visualization, not a detail property |
| 7 | Spec generation on requirement vs. only on story | Both — requirement can generate spec directly or through story, per REQ-REQ-40 |

---

## 15. Open Questions

1. Should the kanban view support drag-and-drop in V1, or should status changes only be available via dropdown/button?
2. Should the priority matrix axes (impact/effort) be configurable, or are they fixed for V1?
3. What is the maximum number of acceptance criteria per requirement?
4. Should the AI analysis run automatically on requirement creation, or only on explicit user action?
5. Should the status distribution strip in the list view be clickable to filter (like a faceted search)?
6. Should bulk operations (REQ-REQ-12) be included in Phase A, or deferred to a later phase?
