# SDD Bootstrap

## Goal

Use the repo in a simple, repeatable `Spec Driven Development` loop before
writing code.

## Recommended Order

Follow this sequence for each slice:

1. `00-context`
2. `01-requirements`
3. `02-user-stories`
4. `03-spec`
5. `04-architecture`
6. `05-design`
7. `06-tasks`
8. code

## Minimum Gate Before Code

Do not start implementation until the slice can answer all of these clearly:

- What problem is this slice solving
- Who is the user or actor
- What is in scope and out of scope
- What must happen in the happy path
- What must happen in error or empty states
- What data shape or state contract is needed
- Which module owns which responsibility
- How acceptance will be checked

## First Slice In This Repo

The first foundation slice is:

- shared app shell

Documents for this slice:

- Context: [project-context.md](/Users/leo/wwa-lab/GitHub/Agentic-SDLC-Control-Tower/docs/00-context/project-context.md:1)
- Stories: [shared-app-shell-stories.md](/Users/leo/wwa-lab/GitHub/Agentic-SDLC-Control-Tower/docs/02-user-stories/shared-app-shell-stories.md:1)
- Spec: [shared-app-shell-spec.md](/Users/leo/wwa-lab/GitHub/Agentic-SDLC-Control-Tower/docs/03-spec/shared-app-shell-spec.md:1)
- Architecture: [shared-app-shell-architecture.md](/Users/leo/wwa-lab/GitHub/Agentic-SDLC-Control-Tower/docs/04-architecture/shared-app-shell-architecture.md:1)
- Tasks: [shared-app-shell-tasks.md](/Users/leo/wwa-lab/GitHub/Agentic-SDLC-Control-Tower/docs/06-tasks/shared-app-shell-tasks.md:1)

## How To Use This Starter

When starting a new slice:

1. duplicate the structure of the shared shell example
2. replace scope, stories, and contracts with the new slice content
3. make sure `03-spec` is the source of truth for implementation behavior
4. only then break work into tasks and start coding

## Simple Rule Of Thumb

If a coding decision cannot be traced back to a story or spec rule yet, the
document alignment is not finished.
