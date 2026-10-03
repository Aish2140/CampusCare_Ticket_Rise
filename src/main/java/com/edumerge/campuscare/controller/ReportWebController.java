package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.service.ReportService;
import com.edumerge.campuscare.util.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class ReportWebController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    public ReportWebController(ReportService reportService, SecurityUtils securityUtils) {
        this.reportService = reportService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String viewReports(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Map<String, Object> report = reportService.generateReport(start, end);
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "reports/index";
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        User currentUser = securityUtils.getCurrentUser();
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"campuscare_tickets.csv\"");
        reportService.exportTicketsCsv(response.getWriter(), currentUser);
    }
}
