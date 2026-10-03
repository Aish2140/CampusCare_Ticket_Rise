package com.edumerge.campuscare.repository;

import com.edumerge.campuscare.entity.Notification;
import com.edumerge.campuscare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findTop10ByUserOrderByCreatedAtDesc(User user);
    long countByUserAndIsReadFalse(User user);
}
