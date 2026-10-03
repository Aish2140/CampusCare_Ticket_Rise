package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.entity.Notification;
import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.service.NotificationService;
import com.edumerge.campuscare.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationWebController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    public NotificationWebController(NotificationService notificationService, SecurityUtils securityUtils) {
        this.notificationService = notificationService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String listNotifications(Model model) {
        User user = securityUtils.getCurrentUser();
        List<Notification> all = notificationService.getAllNotificationsForUser(user);
        model.addAttribute("notifications", all);
        return "notifications/list";
    }

    @GetMapping("/{id}/read")
    public String readAndRedirect(@PathVariable("id") Long id) {
        User user = securityUtils.getCurrentUser();
        notificationService.markAsRead(id, user);
        return "redirect:/notifications";
    }

    @PostMapping("/mark-all-read")
    public String markAllRead() {
        User user = securityUtils.getCurrentUser();
        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }
}
