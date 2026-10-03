package com.edumerge.campuscare.entity;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_number", columnList = "ticketNumber", unique = true),
    @Index(name = "idx_ticket_status", columnList = "status"),
    @Index(name = "idx_ticket_priority", columnList = "priority"),
    @Index(name = "idx_ticket_assigned", columnList = "assigned_to_id"),
    @Index(name = "idx_ticket_requester", columnList = "requester_id"),
    @Index(name = "idx_ticket_created", columnList = "createdAt")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String resolutionSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NextActionOwner nextActionOwner = NextActionOwner.STAFF;

    @Column(nullable = false)
    private LocalDateTime firstResponseDueAt;

    private LocalDateTime firstRespondedAt;

    @Column(nullable = false)
    private LocalDateTime resolutionDueAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime closedAt;

    @Column(nullable = false)
    private int reopenCount = 0;

    @Column(length = 255)
    private String attachmentName;

    @Column(length = 500)
    private String attachmentPath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Ticket() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            if (resolvedAt != null && resolutionDueAt != null) {
                return resolvedAt.isAfter(resolutionDueAt);
            }
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean resolutionBreached = resolutionDueAt != null && now.isAfter(resolutionDueAt);
        boolean firstResponseBreached = firstRespondedAt == null && firstResponseDueAt != null && now.isAfter(firstResponseDueAt);
        return resolutionBreached || firstResponseBreached;
    }

    public SlaStatus getSlaStatus() {
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            return SlaStatus.COMPLETED;
        }
        if (isOverdue()) {
            return SlaStatus.OVERDUE;
        }
        // If remaining time is less than 25% of total time or less than 2 hours
        LocalDateTime now = LocalDateTime.now();
        if (resolutionDueAt != null) {
            long minutesRemaining = Duration.between(now, resolutionDueAt).toMinutes();
            if (minutesRemaining <= 120 && minutesRemaining >= 0) {
                return SlaStatus.APPROACHING_DEADLINE;
            }
        }
        return SlaStatus.ON_TRACK;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    public NextActionOwner getNextActionOwner() {
        return nextActionOwner;
    }

    public void setNextActionOwner(NextActionOwner nextActionOwner) {
        this.nextActionOwner = nextActionOwner;
    }

    public LocalDateTime getFirstResponseDueAt() {
        return firstResponseDueAt;
    }

    public void setFirstResponseDueAt(LocalDateTime firstResponseDueAt) {
        this.firstResponseDueAt = firstResponseDueAt;
    }

    public LocalDateTime getFirstRespondedAt() {
        return firstRespondedAt;
    }

    public void setFirstRespondedAt(LocalDateTime firstRespondedAt) {
        this.firstRespondedAt = firstRespondedAt;
    }

    public LocalDateTime getResolutionDueAt() {
        return resolutionDueAt;
    }

    public void setResolutionDueAt(LocalDateTime resolutionDueAt) {
        this.resolutionDueAt = resolutionDueAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public int getReopenCount() {
        return reopenCount;
    }

    public void setReopenCount(int reopenCount) {
        this.reopenCount = reopenCount;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
