package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.service.UserService;
import com.edumerge.campuscare.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileWebController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    public ProfileWebController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String showProfile(Model model) {
        User user = securityUtils.getCurrentUser();
        model.addAttribute("user", user);
        return "profile/view";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();
        try {
            userService.updateProfile(user.getId(), fullName);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
