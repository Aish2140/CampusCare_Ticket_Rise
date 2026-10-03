package com.edumerge.campuscare.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardStatsDto {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long waitingTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long overdueTickets;
    private double averageResolutionHours;
    private double slaComplianceRate; // Percentage e.g. 92.5%

    private Map<String, Long> categoryDistribution = new HashMap<>();
    private Map<String, Long> priorityDistribution = new HashMap<>();
    private Map<String, Long> statusDistribution = new HashMap<>();
    private Map<String, Long> ageingBuckets = new HashMap<>(); // "<24h", "1-3 days", "3-7 days", ">7 days"

    private List<StaffWorkloadDto> staffWorkloads;

    public DashboardStatsDto() {
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public long getWaitingTickets() {
        return waitingTickets;
    }

    public void setWaitingTickets(long waitingTickets) {
        this.waitingTickets = waitingTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public long getOverdueTickets() {
        return overdueTickets;
    }

    public void setOverdueTickets(long overdueTickets) {
        this.overdueTickets = overdueTickets;
    }

    public double getAverageResolutionHours() {
        return averageResolutionHours;
    }

    public void setAverageResolutionHours(double averageResolutionHours) {
        this.averageResolutionHours = averageResolutionHours;
    }

    public double getSlaComplianceRate() {
        return slaComplianceRate;
    }

    public void setSlaComplianceRate(double slaComplianceRate) {
        this.slaComplianceRate = slaComplianceRate;
    }

    public Map<String, Long> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(Map<String, Long> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public Map<String, Long> getPriorityDistribution() {
        return priorityDistribution;
    }

    public void setPriorityDistribution(Map<String, Long> priorityDistribution) {
        this.priorityDistribution = priorityDistribution;
    }

    public Map<String, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(Map<String, Long> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public Map<String, Long> getAgeingBuckets() {
        return ageingBuckets;
    }

    public void setAgeingBuckets(Map<String, Long> ageingBuckets) {
        this.ageingBuckets = ageingBuckets;
    }

    public List<StaffWorkloadDto> getStaffWorkloads() {
        return staffWorkloads;
    }

    public void setStaffWorkloads(List<StaffWorkloadDto> staffWorkloads) {
        this.staffWorkloads = staffWorkloads;
    }
}
