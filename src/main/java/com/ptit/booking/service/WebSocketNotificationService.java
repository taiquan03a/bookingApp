package com.ptit.booking.service;

public interface WebSocketNotificationService {
    void sendNotificationToUser(Long userId, String title, String message);
}
