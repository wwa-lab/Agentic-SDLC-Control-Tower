package com.sdlctower.platform.navigation;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NavigationService {

    private static final List<NavItem> NAV_ITEMS = List.of(
            new NavItem("dashboard", "Dashboard", "/", false),
            new NavItem("team", "Team Space", "/team", true),
            new NavItem("project-space", "Project Space", "/project-space", false),
            new NavItem("requirements", "Requirement Management", "/requirements", true),
            new NavItem("project-management", "Project Management", "/project-management", true),
            new NavItem("design", "Design Management", "/design", true),
            new NavItem("code", "Code & Build", "/code", true),
            new NavItem("testing", "Testing", "/testing", true),
            new NavItem("deployment", "Deployment", "/deployment", true),
            new NavItem("incidents", "Incident Management", "/incidents", false),
            new NavItem("ai-center", "AI Center", "/ai-center", true),
            new NavItem("reports", "Report Center", "/reports", true),
            new NavItem("platform", "Platform Center", "/platform", false)
    );

    public List<NavItem> getEntries() {
        return NAV_ITEMS;
    }
}
