package com.edumerge.campuscare.entity;

public enum SlaStatus {
    ON_TRACK("On Track", "badge-sla-ontrack", "success"),
    APPROACHING_DEADLINE("Approaching Deadline", "badge-sla-warning", "warning"),
    OVERDUE("Overdue", "badge-sla-breached", "danger"),
    COMPLETED("Completed", "badge-sla-completed", "secondary");

    private final String displayName;
    private final String cssClass;
    private final String bsColor;

    SlaStatus(String displayName, String cssClass, String bsColor) {
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
