package com.edumerge.campuscare.service;

import com.edumerge.campuscare.entity.Category;
import com.edumerge.campuscare.entity.Department;
import com.edumerge.campuscare.exception.ResourceNotFoundException;
import com.edumerge.campuscare.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DepartmentService departmentService;

    public CategoryService(CategoryRepository categoryRepository, DepartmentService departmentService) {
        this.categoryRepository = categoryRepository;
        this.departmentService = departmentService;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public List<Category> getCategoriesByDepartment(Long departmentId) {
        return categoryRepository.findByDepartmentIdAndIsActiveTrue(departmentId);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public Category createCategory(String name, String description, Long departmentId) {
        Department department = departmentService.getDepartmentById(departmentId);
        Category category = new Category(name, description, department);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description, Long departmentId, boolean isActive) {
        Category category = getCategoryById(id);
        Department department = departmentService.getDepartmentById(departmentId);
        category.setName(name);
        category.setDescription(description);
        category.setDepartment(department);
        category.setActive(isActive);
        return categoryRepository.save(category);
    }
}
