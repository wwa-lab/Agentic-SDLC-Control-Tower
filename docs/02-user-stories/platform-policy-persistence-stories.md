# Platform Policy Persistence User Stories

## Epic: Durable Policy Governance

### S-PPP-01: Browse persisted policies

> As a Platform Administrator  
> I want the Policies catalog to read from persisted policy rows  
> So that policy governance survives backend restarts.

**Acceptance Criteria**

- Given policies exist in `PLATFORM_POLICY`
  When I open the Policies page
  Then rows show key, name, category, scope, status, bound-to, and version.

- Given I filter by category, status, scope, bound-to, or search text
  When matching rows exist
  Then only matching persisted rows are returned.

### S-PPP-02: Create a policy

> As a Platform Administrator  
> I want to create a policy  
> So that a new governance rule is available for downstream consumers.

**Acceptance Criteria**

- Given I submit a valid policy body
  When creation succeeds
  Then the policy is persisted as version `1` and Audit contains
  `policy.create`.

- Given a duplicate active policy key/scope exists
  When I submit the same key/scope
  Then the API returns `409 POLICY_ALREADY_EXISTS`.

### S-PPP-03: Edit a policy as a new version

> As a Platform Administrator  
> I want edits to produce a new policy version  
> So that policy history remains traceable.

**Acceptance Criteria**

- Given policy `P` is version `1` and active
  When I edit its body
  Then a new policy row is created at version `2`, the old row becomes
  inactive, and Audit contains `policy.update`.

### S-PPP-04: Activate or deactivate a policy

> As a Platform Administrator  
> I want to activate or deactivate a policy  
> So that I can manage rollout without deleting policy history.

**Acceptance Criteria**

- Given a policy is active
  When I deactivate it
  Then the row remains present and Audit contains `policy.deactivate`.

- Given a policy is inactive
  When I activate it
  Then the row status becomes active and Audit contains `policy.activate`.

### S-PPP-05: Add and revoke policy exceptions

> As a Platform Administrator  
> I want to add time-boxed exceptions  
> So that I can unblock special cases without disabling the whole policy.

**Acceptance Criteria**

- Given an active policy exists
  When I add an exception with a future expiry
  Then the exception appears under that policy and Audit contains
  `policy.exception.add`.

- Given an exception exists
  When I revoke it
  Then `revokedAt` is set and Audit contains `policy.exception.revoke`.
