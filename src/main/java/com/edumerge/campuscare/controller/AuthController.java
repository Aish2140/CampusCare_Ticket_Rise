package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam("fullName") String fullName,
                                 @RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam("studentId") String studentId,
                                 Model model) {
        try {
            userService.registerStudent(fullName, email, password, studentId);
            return "redirect:/login?registered=true";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String redirectDashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(ga.getAuthority())) {
                return "redirect:/admin/dashboard";
            } else if ("ROLE_STAFF".equals(ga.getAuthority())) {
                return "redirect:/staff/dashboard";
            } else if ("ROLE_STUDENT".equals(ga.getAuthority())) {
                return "redirect:/student/dashboard";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
