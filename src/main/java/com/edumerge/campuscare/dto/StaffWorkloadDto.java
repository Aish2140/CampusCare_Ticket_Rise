package com.edumerge.campuscare.dto;

public class StaffWorkloadDto {
    private Long staffId;
    private String staffName;
    private String email;
    private String departmentName;
    private long openTicketsCount;
    private long totalAssignedCount;
    private long overdueCount;
    private boolean active;

    public StaffWorkloadDto() {
    }

    public StaffWorkloadDto(Long staffId, String staffName, String email, String departmentName, long openTicketsCount, long totalAssignedCount, long overdueCount, boolean active) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.email = email;
        this.departmentName = departmentName;
        this.openTicketsCount = openTicketsCount;
        this.totalAssignedCount = totalAssignedCount;
        this.overdueCount = overdueCount;
        this.active = active;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public long getOpenTicketsCount() {
        return openTicketsCount;
    }

    public void setOpenTicketsCount(long openTicketsCount) {
        this.openTicketsCount = openTicketsCount;
    }

    public long getTotalAssignedCount() {
        return totalAssignedCount;
    }

    public void setTotalAssignedCount(long totalAssignedCount) {
        this.totalAssignedCount = totalAssignedCount;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
