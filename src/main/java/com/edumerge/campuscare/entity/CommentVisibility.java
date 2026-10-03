package com.edumerge.campuscare.entity;

public enum CommentVisibility {
    PUBLIC("Public Reply"),
    INTERNAL("Internal Note");

    private final String displayName;

    CommentVisibility(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
