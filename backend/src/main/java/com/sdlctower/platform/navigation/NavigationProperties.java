package com.sdlctower.platform.navigation;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized navigation configuration.
 * Navigation entries are defined in application.yml under {@code app.navigation.items}.
 */
@ConfigurationProperties(prefix = "app.navigation")
public record NavigationProperties(List<NavItem> items) {

    public NavigationProperties {
        items = items != null ? List.copyOf(items) : List.of();
    }
}
