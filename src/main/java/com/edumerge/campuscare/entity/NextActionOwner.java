package com.edumerge.campuscare.entity;

public enum NextActionOwner {
    STUDENT("Student"),
    STAFF("Support Staff"),
    ADMIN("Admin / Manager");

    private final String displayName;

    NextActionOwner(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
