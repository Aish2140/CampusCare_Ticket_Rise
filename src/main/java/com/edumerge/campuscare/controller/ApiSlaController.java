package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.dto.ApiResponse;
import com.edumerge.campuscare.entity.SlaPolicy;
import com.edumerge.campuscare.service.SlaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sla")
public class ApiSlaController {

    private final SlaService slaService;

    public ApiSlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<List<SlaPolicy>>> getPolicies() {
        return ResponseEntity.ok(ApiResponse.ok(slaService.getAllPolicies()));
    }
}
