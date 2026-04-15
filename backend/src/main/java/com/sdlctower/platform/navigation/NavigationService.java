package com.sdlctower.platform.navigation;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NavigationService {

    private static final List<NavItem> NAV_ITEMS = List.of(
            new NavItem("dashboard", "Dashboard", "/", false, "LayoutDashboard", 1),
            new NavItem("team", "Team Space", "/team", true, "Users", 2),
            new NavItem("project-space", "Project Space", "/project-space", false, "Box", 3),
            new NavItem("requirements", "Requirement Management", "/requirements", true, "FileText", 4),
            new NavItem("project-management", "Project Management", "/project-management", true, "GitBranch", 5),
            new NavItem("design", "Design Management", "/design", true, "Layers", 6),
            new NavItem("code", "Code & Build", "/code", true, "Code", 7),
            new NavItem("testing", "Testing", "/testing", true, "TestTube", 8),
            new NavItem("deployment", "Deployment", "/deployment", true, "Send", 9),
            new NavItem("incidents", "Incident Management", "/incidents", false, "AlertTriangle", 10),
            new NavItem("ai-center", "AI Center", "/ai-center", true, "Cpu", 11),
            new NavItem("reports", "Report Center", "/reports", true, "BarChart", 12),
            new NavItem("platform", "Platform Center", "/platform", false, "Settings", 13)
    );

    public List<NavItem> getEntries() {
        return NAV_ITEMS;
    }
}
