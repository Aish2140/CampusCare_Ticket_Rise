package com.edumerge.campuscare.service;

import com.edumerge.campuscare.entity.Notification;
import com.edumerge.campuscare.entity.Ticket;
import com.edumerge.campuscare.entity.User;
import com.edumerge.campuscare.exception.ResourceNotFoundException;
import com.edumerge.campuscare.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification sendNotification(User recipient, Ticket ticket, String type, String message) {
        if (recipient == null) return null;
        Notification notification = new Notification(recipient, ticket, type, message);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getAllNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (notification.getUser().getId().equals(user.getId())) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification n : unread) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(unread);
    }
}
