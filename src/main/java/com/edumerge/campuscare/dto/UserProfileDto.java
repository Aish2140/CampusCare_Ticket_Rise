package com.edumerge.campuscare.dto;

import com.edumerge.campuscare.entity.Role;

public class UserProfileDto {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String departmentName;
    private String studentOrEmployeeId;
    private boolean active;

    public UserProfileDto() {
    }

    public UserProfileDto(Long id, String fullName, String email, Role role, String departmentName, String studentOrEmployeeId, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.departmentName = departmentName;
        this.studentOrEmployeeId = studentOrEmployeeId;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getStudentOrEmployeeId() {
        return studentOrEmployeeId;
    }

    public void setStudentOrEmployeeId(String studentOrEmployeeId) {
        this.studentOrEmployeeId = studentOrEmployeeId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
