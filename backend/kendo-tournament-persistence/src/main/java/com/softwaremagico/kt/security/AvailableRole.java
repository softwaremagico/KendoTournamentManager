package com.softwaremagico.kt.security;

public enum AvailableRole {
    ROLE_VIEWER,
    ROLE_ADMIN;

    private static final String ROLE_PREFIX = "ROLE_";

    public static AvailableRole get(String roleName) {
        for (final AvailableRole availableRole : AvailableRole.values()) {
            if (availableRole.name().equalsIgnoreCase(ROLE_PREFIX + roleName)) {
                return availableRole;
            }
        }
        return null;
    }
}
