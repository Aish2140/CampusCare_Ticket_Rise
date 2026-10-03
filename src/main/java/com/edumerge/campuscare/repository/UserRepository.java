package com.edumerge.campuscare.repository;

import com.edumerge.campuscare.entity.Department;
import com.edumerge.campuscare.entity.Role;
import com.edumerge.campuscare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndIsActiveTrue(Role role);
    List<User> findByRoleAndDepartmentAndIsActiveTrue(Role role, Department department);
    
    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.fullName ASC")
    List<User> findAllByRoleOrderByName(Role role);
}
