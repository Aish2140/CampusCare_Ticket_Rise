package com.edumerge.campuscare.repository;

import com.edumerge.campuscare.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIsActiveTrue();
    List<Category> findByDepartmentIdAndIsActiveTrue(Long departmentId);
    Optional<Category> findByNameIgnoreCase(String name);
}
