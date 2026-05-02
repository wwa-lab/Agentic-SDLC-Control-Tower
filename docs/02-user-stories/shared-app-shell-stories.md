# Shared App Shell Stories

## Scope

This document defines the first simple SDD slice for implementation:

- the shared application shell used by Round 1 pages

It is the minimum foundation needed before deeper page modules are built.

## Story S1: Enter Any Business Page Through A Consistent Shell

As a user  
I want every major page to use the same shell structure  
So that I can navigate the product without relearning the frame each time

### Acceptance Criteria

- Every Round 1 page renders the same left navigation structure
- Every Round 1 page renders the same top context bar structure
- Every Round 1 page reserves a persistent right-side AI Command Panel region
- The active page is visually distinguishable in the left navigation
- The shell remains usable for both overview pages and detail-heavy pages

### Edge Notes

- Content inside the page body may differ by route
- The shell should not force each page to duplicate layout markup

## Story S2: Understand Current Context At A Glance

As a user  
I want the current business context to be visible at the top of the page  
So that I know which workspace and delivery scope I am looking at

### Acceptance Criteria

- The top context bar shows `Workspace / Application / SNOW Group / Project / Environment`
- Missing optional values are rendered safely without breaking layout
- Context display works consistently across Dashboard, Project Space, Incident Management, and Platform Center

### Edge Notes

- Organizations without SNOW Group usage may show an empty or fallback value
- The shell should distinguish context display from page-specific content

## Story S3: Reach Major Product Areas From Primary Navigation

As a user  
I want a stable primary navigation  
So that I can move across the major SDLC areas quickly

### Acceptance Criteria

- The navigation exposes the 13 PRD page entries
- The current route maps to exactly one active navigation item
- Navigation labels use the agreed product vocabulary from the PRD
- The shared shell allows later route linking without structural rewrites

### Navigation Set

- Dashboard
- Team Space
- Project Space
- Requirement Management
- Project Management
- Design Management
- Code & Build
- Testing
- Deployment
- Incident Management
- AI Center
- Report Center
- Platform Center

## Story S4: See AI As A Native Operating Surface

As a user  
I want a persistent AI Command Panel region in the shell  
So that AI feels like part of the product surface rather than a separate tool

### Acceptance Criteria

- The shell reserves a right-side panel region on supported desktop layouts
- The panel supports summary, reasoning, evidence, and action content areas
- The panel is layout-stable even when page body content changes
- The shell can render page-scoped AI content without redefining the panel structure on each page

### Edge Notes

- This foundation slice does not define full AI behavior
- This slice only defines the container, regions, and integration contract

## Story S5: Access Global Utility Actions

As a user  
I want consistent global utility entry points  
So that search, notifications, and audit are always reachable

### Acceptance Criteria

- The shell includes global search entry
- The shell includes notifications entry
- The shell includes audit/history entry
- These utilities appear in a consistent area of the shell

## Story S6: Log In With Staff ID

As a staff user
I want to log in with my staff id and a non-empty password
So that the system can apply my team scope and permissions

### Acceptance Criteria

- The login screen accepts staff id such as `43910516`
- Password is required and cannot be empty
- Successful login creates a current-user session with staff id, roles, and scope grants
- Failed login shows an inline error without clearing the staff id field

## Story S6A: Log In With TeamBook SSO In Internal Deployments

As an internal staff user
I want to enter the platform through TeamBook SSO when it is available
So that I do not need a separate platform password and the shell can show my company profile

### Acceptance Criteria

- Internal environments can show a TeamBook SSO entry point before the manual staff-id form
- TeamBook callback maps the company identity to staff id, nStaff Name, and avatar URL
- The current-user session includes `authProvider = "teambook"` and profile fields when TeamBook returns them
- TeamBook authentication does not grant access by itself; the staff id must map to an active `PlatformUser` with role assignments
- Local or external environments can hide TeamBook and continue to use manual staff-id login and guest mode

## Story S7: Browse In Guest Mode

As a visitor
I want to continue as a guest
So that I can understand the system's core capabilities without requesting access first

### Acceptance Criteria

- The login screen includes a "Continue as Guest" action
- Guest mode enters the normal shell and navigation
- Guest mode uses read-only demo data, not real Application + SNOW team data
- Mutating actions are hidden or disabled with a read-only reason
- API write attempts from a guest return 403

## Story S8: Raise A Support Issue From Contact Us

As a user or guest
I want to submit a Contact Us form
So that a support story is created in the internal Jira project

### Acceptance Criteria

- The shell has a global Contact Us button
- Clicking it opens a form with title, category, and description
- Submitting the form sends current route, current context, reporter identity, and timestamp
- On success, the UI shows the created Jira key and link
- On failure, the form remains open and shows retry guidance

## Story S9: Open User Guideline

As a user or guest
I want a User Guideline button
So that I can jump to the official Confluence guideline page

### Acceptance Criteria

- The shell has a global User Guideline button
- Clicking it opens the configured Confluence URL in a new tab
- If the URL is missing, the button is disabled with a tooltip

## Assumptions

- Desktop-first; mobile layout is out of scope for V1 shell
- Shell renders with either authenticated user context or guest demo context
- All 13 navigation entries are rendered for authenticated users and guests; mutating actions obey role/mode restrictions
- Vue 3 with Composition API and `<script setup>` is the frontend implementation target
- AI Command Panel is a layout container only; behavior and content logic are deferred

## Dependencies

- PRD V0.9 navigation vocabulary (§10 information architecture) is accepted
- `WorkspaceContext` field contract is accepted before context bar implementation
- Vue Router setup must exist before route-to-nav mapping can be wired
- Design HTML prototypes (Round 1) provide visual reference but are not binding

## Open Questions

- Should the nav labels use the full PRD vocabulary (e.g., "Code & Build Management") or shortened forms (e.g., "Code & Build")?
- Should the shell support a collapsed/minimized navigation state in V1?
- What is the fallback behavior when the AI Command Panel has no page-scoped content?
- What exact Jira project key and Confluence guideline URL should each environment use?

## Out Of Scope For This Slice

- page-specific business widgets
- live data integration
- full enterprise SSO / directory sync
- mobile layout optimization
- full AI action workflow behavior
- design token finalization
