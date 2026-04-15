package com.sdlctower.platform.navigation;

/**
 * Navigation item returned by GET /api/v1/nav/entries.
 * Includes icon identifier and display order per task B2.
 */
public record NavItem(
        String key,
        String label,
        String path,
        boolean comingSoon,
        String icon,
        int order
) {
}
