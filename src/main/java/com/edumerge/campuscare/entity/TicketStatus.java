package com.edumerge.campuscare.entity;

public enum TicketStatus {
    OPEN("Open", "badge-open", "primary"),
    IN_PROGRESS("In Progress", "badge-progress", "info"),
    WAITING_FOR_STUDENT("Waiting for Student", "badge-waiting", "warning"),
    RESOLVED("Resolved", "badge-resolved", "success"),
    CLOSED("Closed", "badge-closed", "secondary");

    private final String displayName;
    private final String cssClass;
    private final String bsColor;

    TicketStatus(String displayName, String cssClass, String bsColor) {
        this.displayName = displayName;
        this.cssClass = cssClass;
        this.bsColor = bsColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getBsColor() {
        return bsColor;
    }
}
