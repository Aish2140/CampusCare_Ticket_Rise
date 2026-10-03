package com.edumerge.campuscare.dto;

import com.edumerge.campuscare.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TicketCreateDto {

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 255, message = "Subject must be between 5 and 255 characters")
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Please provide at least 10 characters describing the issue")
    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    public TicketCreateDto() {
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
}
