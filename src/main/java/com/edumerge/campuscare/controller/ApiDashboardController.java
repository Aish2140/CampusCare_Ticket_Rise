package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.dto.ApiResponse;
import com.edumerge.campuscare.dto.DashboardStatsDto;
import com.edumerge.campuscare.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class ApiDashboardController {

    private final DashboardService dashboardService;

    public ApiDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getAdminStats() {
        DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
