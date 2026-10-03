package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.dto.StaffWorkloadDto;
import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.service.DepartmentService;
import com.edumerge.campuscare.service.TicketService;
import com.edumerge.campuscare.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/staff")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class StaffWebController {

    private final UserService userService;
    private final DepartmentService departmentService;
    private final TicketService ticketService;

    public StaffWebController(UserService userService, DepartmentService departmentService, TicketService ticketService) {
        this.userService = userService;
        this.departmentService = departmentService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public String staffDirectory(Model model) {
        List<User> staffMembers = userService.getAllStaff();
        model.addAttribute("staffList", staffMembers);
        model.addAttribute("departments", departmentService.getActiveDepartments());
        return "admin/staff";
    }

    @PostMapping("/create")
    public String createStaff(@RequestParam("fullName") String fullName,
                              @RequestParam("email") String email,
                              @RequestParam("password") String password,
                              @RequestParam("departmentId") Long departmentId,
                              @RequestParam("employeeId") String employeeId,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.createStaff(fullName, email, password, departmentId, employeeId);
            redirectAttributes.addFlashAttribute("successMessage", "Staff account created successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStaffStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            User updated = userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Staff account " + (updated.isActive() ? "activated" : "deactivated") + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/{id}/department")
    public String updateDepartment(@PathVariable("id") Long id,
                                   @RequestParam("departmentId") Long departmentId,
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.updateStaffDepartment(id, departmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Staff department updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/staff";
    }
}
