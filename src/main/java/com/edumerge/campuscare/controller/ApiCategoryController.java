package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.dto.ApiResponse;
import com.edumerge.campuscare.entity.Category;
import com.edumerge.campuscare.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class ApiCategoryController {

    private final CategoryService categoryService;

    public ApiCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getActiveCategories()));
    }
}
