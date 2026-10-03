package com.edumerge.campuscare.dto;

import com.edumerge.campuscare.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class TicketStatusUpdateDto {

    @NotNull(message = "Status is required")
    private TicketStatus status;

    private String note;

    public TicketStatusUpdateDto() {
    }

    public TicketStatusUpdateDto(TicketStatus status, String note) {
        this.status = status;
        this.note = note;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
