package com.ptit.booking.service;

public interface WebSocketNotificationService {
    void sendNotificationToUser(Long userId,Long notificationId, String title, String message);
}
