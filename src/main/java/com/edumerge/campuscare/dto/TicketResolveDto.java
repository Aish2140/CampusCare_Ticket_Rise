package com.edumerge.campuscare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketResolveDto {

    @NotBlank(message = "Resolution summary is required")
    @Size(min = 5, message = "Resolution summary must be at least 5 characters")
    private String resolutionSummary;

    public TicketResolveDto() {
    }

    public TicketResolveDto(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }
}
