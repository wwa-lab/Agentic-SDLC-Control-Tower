package com.sdlctower.platform.navigation;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NavigationService {

    private final List<NavItem> navItems;

    public NavigationService(NavigationProperties properties) {
        this.navItems = properties.items();
    }

    public List<NavItem> getEntries() {
        return navItems;
    }
}
