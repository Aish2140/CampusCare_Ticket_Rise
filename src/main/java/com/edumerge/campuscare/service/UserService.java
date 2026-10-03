package com.edumerge.campuscare.service;

import com.edumerge.campuscare.entity.Department;
import com.edumerge.campuscare.entity.Role;
import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.exception.ResourceNotFoundException;
import com.edumerge.campuscare.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, DepartmentService departmentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<User> getAllStaff() {
        return userRepository.findAllByRoleOrderByName(Role.ROLE_STAFF);
    }

    public List<User> getActiveStaff() {
        return userRepository.findByRoleAndIsActiveTrue(Role.ROLE_STAFF);
    }

    public List<User> getActiveStaffByDepartment(Department department) {
        return userRepository.findByRoleAndDepartmentAndIsActiveTrue(Role.ROLE_STAFF, department);
    }

    public List<User> getAllStudents() {
        return userRepository.findAllByRoleOrderByName(Role.ROLE_STUDENT);
    }

    @Transactional
    public User registerStudent(String fullName, String email, String rawPassword, String studentId) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        User student = new User(
                fullName,
                email.toLowerCase().trim(),
                passwordEncoder.encode(rawPassword),
                Role.ROLE_STUDENT,
                null,
                studentId
        );
        return userRepository.save(student);
    }

    @Transactional
    public User createStaff(String fullName, String email, String rawPassword, Long departmentId, String employeeId) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        Department dept = departmentId != null ? departmentService.getDepartmentById(departmentId) : null;
        User staff = new User(
                fullName,
                email.toLowerCase().trim(),
                passwordEncoder.encode(rawPassword),
                Role.ROLE_STAFF,
                dept,
                employeeId
        );
        return userRepository.save(staff);
    }

    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = getById(userId);
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    @Transactional
    public User updateStaffDepartment(Long userId, Long departmentId) {
        User user = getById(userId);
        Department dept = departmentId != null ? departmentService.getDepartmentById(departmentId) : null;
        user.setDepartment(dept);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Long userId, String fullName) {
        User user = getById(userId);
        user.setFullName(fullName);
        return userRepository.save(user);
    }
}
