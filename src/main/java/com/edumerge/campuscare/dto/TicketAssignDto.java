package com.edumerge.campuscare.dto;

import jakarta.validation.constraints.NotNull;

public class TicketAssignDto {

    @NotNull(message = "Staff member ID is required")
    private Long staffId;

    public TicketAssignDto() {
    }

    public TicketAssignDto(Long staffId) {
        this.staffId = staffId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }
}
