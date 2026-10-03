package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.dto.DashboardStatsDto;
import com.edumerge.campuscare.entity.Ticket;
import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.service.DashboardService;
import com.edumerge.campuscare.service.TicketService;
import com.edumerge.campuscare.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final TicketService ticketService;
    private final SecurityUtils securityUtils;

    public DashboardController(DashboardService dashboardService, TicketService ticketService, SecurityUtils securityUtils) {
        this.dashboardService = dashboardService;
        this.ticketService = ticketService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminDashboard(Model model) {
        DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
        model.addAttribute("stats", stats);
        return "dashboard/admin";
    }

    @GetMapping("/staff/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')")
    public String staffDashboard(Model model) {
        User staff = securityUtils.getCurrentUser();
        Map<String, Object> stats = dashboardService.getStaffDashboardStats(staff);
        model.addAttribute("stats", stats);
        return "dashboard/staff";
    }

    @GetMapping("/student/dashboard")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public String studentDashboard(Model model) {
        User student = securityUtils.getCurrentUser();
        Map<String, Object> stats = dashboardService.getStudentDashboardStats(student);
        model.addAttribute("stats", stats);
        return "dashboard/student";
    }
}
