package com.ptit.booking.service;

import com.ptit.booking.enums.EnumNotificationType;
import com.ptit.booking.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface NotificationService {
    void sendNotification(Long userId, String title, String message, EnumNotificationType type);
    Page<Notification> getUserNotifications(Principal principal, Pageable pageable);
}
