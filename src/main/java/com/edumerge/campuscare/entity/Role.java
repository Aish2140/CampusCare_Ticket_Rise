package com.edumerge.campuscare.entity;

public enum Role {
    ROLE_STUDENT("Student"),
    ROLE_STAFF("Support Staff"),
    ROLE_ADMIN("Admin / Manager");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
