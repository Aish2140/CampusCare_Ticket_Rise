package com.edumerge.campuscare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketReopenDto {

    @NotBlank(message = "Reason for reopening is required")
    @Size(min = 5, message = "Reason must be at least 5 characters")
    private String reason;

    public TicketReopenDto() {
    }

    public TicketReopenDto(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
