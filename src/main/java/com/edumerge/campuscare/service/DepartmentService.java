package com.edumerge.campuscare.service;

import com.edumerge.campuscare.entity.Department;
import com.edumerge.campuscare.exception.ResourceNotFoundException;
import com.edumerge.campuscare.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Department> getActiveDepartments() {
        return departmentRepository.findByIsActiveTrue();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Transactional
    public Department createDepartment(String name, String description) {
        Department dept = new Department(name, description);
        return departmentRepository.save(dept);
    }

    @Transactional
    public Department updateDepartment(Long id, String name, String description, boolean isActive) {
        Department dept = getDepartmentById(id);
        dept.setName(name);
        dept.setDescription(description);
        dept.setActive(isActive);
        return departmentRepository.save(dept);
    }
}
