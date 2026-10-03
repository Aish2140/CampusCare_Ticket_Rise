package com.edumerge.campuscare.controller;

import com.edumerge.campuscare.entity.Notification;
import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.service.NotificationService;
import com.edumerge.campuscare.util.SecurityUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

    public GlobalModelAttributes(SecurityUtils securityUtils, NotificationService notificationService) {
        this.securityUtils = securityUtils;
        this.notificationService = notificationService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        return securityUtils.getCurrentUser();
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount() {
        User user = securityUtils.getCurrentUser();
        return user != null ? notificationService.getUnreadCount(user) : 0;
    }

    @ModelAttribute("navbarNotifications")
    public List<Notification> getNavbarNotifications() {
        User user = securityUtils.getCurrentUser();
        return user != null ? notificationService.getNotificationsForUser(user) : Collections.emptyList();
    }
}
