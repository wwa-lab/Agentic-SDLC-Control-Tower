# Platform Access + Audit Persistence User Stories

## Epic: Persistent Access Administration

### S-PAA-01: Create a durable staff user

> As a Platform Administrator  
> I want to create a staff user by staff id  
> So that the user can receive scoped roles and remain available after restart.

**Acceptance Criteria**

- Given I create staff id `43920001` with display name and email
  When the backend restarts
  Then `GET /api/v1/platform/access/users?q=43920001` still returns that user.

- Given the user is created successfully
  When I open the Audit page
  Then I can find a `permission_change` event with action `user.create`.

### S-PAA-02: Update or deactivate a staff user

> As a Platform Administrator  
> I want to update profile metadata or deactivate a staff user  
> So that access records stay current without losing history.

**Acceptance Criteria**

- Given an active user exists
  When I update their display name or email
  Then the user catalog shows the new metadata and Audit contains `user.update`.

- Given an active user exists
  When I set status to `inactive`
  Then future logins for that staff id are rejected and Audit contains
  `user.deactivate`.

### S-PAA-03: Assign a durable role

> As a Platform Administrator  
> I want to assign a scoped role to an active user  
> So that consumer slices can enforce access using persisted grants.

**Acceptance Criteria**

- Given an active user exists
  When I grant `WORKSPACE_VIEWER` at `workspace:ws-default-001`
  Then the assignment appears in `GET /platform/access/assignments` after restart.

- Given the grant succeeds
  When I inspect Audit
  Then there is a `permission_change` event with action `role.grant`.

- Given the same assignment already exists
  When I submit the same grant again
  Then the API returns the existing assignment or a deterministic conflict without
  creating a duplicate row.

### S-PAA-04: Revoke a durable role

> As a Platform Administrator  
> I want to revoke an assignment  
> So that stale access is removed and the removal is traceable.

**Acceptance Criteria**

- Given an assignment exists
  When I revoke it
  Then the assignment no longer appears in the assignment catalog.

- Given the revoke succeeds
  When I inspect Audit
  Then there is a `permission_change` event with action `role.revoke`.

### S-PAA-05: Prevent platform lockout

> As a Platform Administrator  
> I want the system to block removal of the final active Platform Admin  
> So that the platform cannot be locked out.

**Acceptance Criteria**

- Given only one active `PLATFORM_ADMIN` assignment remains
  When I revoke that assignment
  Then the API returns `409 LAST_PLATFORM_ADMIN` and no assignment is deleted.

- Given only one active `PLATFORM_ADMIN` user remains
  When I deactivate that user
  Then the API returns `409 LAST_PLATFORM_ADMIN` and the user remains active.

- Given the operation is blocked
  When I inspect Audit
  Then no success audit row is written for the rejected mutation.

### S-PAA-06: Browse real access audit history

> As a Platform Administrator  
> I want the Audit page to show access events created by real mutations  
> So that I can trace who changed access and when.

**Acceptance Criteria**

- Given several access mutations have occurred
  When I filter Audit by category `permission_change`
  Then the records include user and role mutation actions.

- Given I filter Audit by actor or object id
  When matching rows exist
  Then only matching persisted audit rows are returned.
