# Platform Configuration Persistence User Stories

## Epic: Durable Platform Configuration

### S-PCP-01: Browse persisted templates

> As a Platform Administrator  
> I want the Templates catalog to read from persisted template rows  
> So that the page reflects the authoritative platform template catalog.

**Acceptance Criteria**

- Given seed templates exist in `PLATFORM_TEMPLATE`
  When I open the Templates page
  Then the catalog renders those persisted templates.

- Given a template id does not exist
  When I request its detail
  Then the API returns 404.

### S-PCP-02: Inspect persisted template versions

> As a Platform Administrator  
> I want to inspect version bodies and version history  
> So that I can understand what a template currently defines.

**Acceptance Criteria**

- Given a template has one or more versions
  When I open its detail
  Then I see the current version and version history from
  `PLATFORM_TEMPLATE_VERSION`.

### S-PCP-03: Browse persisted configurations

> As a Platform Administrator  
> I want the Configurations catalog to read from persisted rows  
> So that configuration governance survives backend restarts.

**Acceptance Criteria**

- Given configuration rows exist in `PLATFORM_CONFIGURATION`
  When I open the Configurations page
  Then I see rows grouped by kind, scope, status, drift, and last modified time.

- Given I filter by scope or kind
  When matching rows exist
  Then only matching persisted rows are returned.

### S-PCP-04: Create a configuration override

> As a Platform Administrator  
> I want to create a scoped configuration override  
> So that a workspace or application can deviate from the platform default.

**Acceptance Criteria**

- Given platform-default configuration `shell.nav.density` exists
  When I create a workspace override for `ws-default-001`
  Then a row is inserted with parent id pointing at the platform default.

- Given creation succeeds
  When I inspect Audit
  Then there is a `config_change` event with action `configuration.create`.

### S-PCP-05: Update or deactivate a configuration

> As a Platform Administrator  
> I want to update or deactivate a configuration  
> So that stale overrides can be changed without deleting history.

**Acceptance Criteria**

- Given a configuration exists
  When I update its JSON body
  Then detail returns the updated body and Audit contains `configuration.update`.

- Given an active configuration exists
  When I set status to `inactive`
  Then the row remains present and Audit contains `configuration.deactivate`.

### S-PCP-06: See drift from platform default

> As a Platform Administrator  
> I want detail to show drift fields against the platform default  
> So that I can see which keys were overridden.

**Acceptance Criteria**

- Given a scoped override body differs from the platform default body
  When I open configuration detail
  Then `hasDrift` is true and `driftFields` lists the changed top-level fields.

- Given the bodies are equivalent
  When I open configuration detail
  Then `hasDrift` is false and `driftFields` is empty.
