package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.dto.ApiResponse;
import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class ApiStaffController {

    private final UserService userService;

    public ApiStaffController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getActiveStaff() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getActiveStaff()));
    }
}
