package com.edumerge.campuscare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private TicketPriority priority;

    @Column(nullable = false)
    private int firstResponseMinutes;

    @Column(nullable = false)
    private int resolutionMinutes;

    @Column(nullable = false)
    private boolean pauseWhileWaiting = true;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public SlaPolicy() {
    }

    public SlaPolicy(TicketPriority priority, int firstResponseMinutes, int resolutionMinutes, boolean pauseWhileWaiting) {
        this.priority = priority;
        this.firstResponseMinutes = firstResponseMinutes;
        this.resolutionMinutes = resolutionMinutes;
        this.pauseWhileWaiting = pauseWhileWaiting;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public int getFirstResponseMinutes() {
        return firstResponseMinutes;
    }

    public void setFirstResponseMinutes(int firstResponseMinutes) {
        this.firstResponseMinutes = firstResponseMinutes;
    }

    public int getResolutionMinutes() {
        return resolutionMinutes;
    }

    public void setResolutionMinutes(int resolutionMinutes) {
        this.resolutionMinutes = resolutionMinutes;
    }

    public boolean isPauseWhileWaiting() {
        return pauseWhileWaiting;
    }

    public void setPauseWhileWaiting(boolean pauseWhileWaiting) {
        this.pauseWhileWaiting = pauseWhileWaiting;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
}
