package com.ptit.booking.service;

import com.ptit.booking.enums.EnumNotificationType;

import java.time.LocalDateTime;

public interface WebSocketNotificationService {
    void sendNotificationToUser(
            Long userId, Long notificationId, String title, String message,
            EnumNotificationType type,boolean isRead, LocalDateTime createAt);
}
