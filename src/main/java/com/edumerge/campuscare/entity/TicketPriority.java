package com.edumerge.campuscare.entity;

public enum TicketPriority {
    LOW("Low", "badge-prio-low", "secondary"),
    MEDIUM("Medium", "badge-prio-med", "primary"),
    HIGH("High", "badge-prio-high", "warning"),
    URGENT("Urgent", "badge-prio-urgent", "danger");

    private final String displayName;
    private final String cssClass;
    private final String bsColor;

    TicketPriority(String displayName, String cssClass, String bsColor) {
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
