package com.ptit.booking.service.Impl;

import com.ptit.booking.dto.notification.NotificationDto;
import com.ptit.booking.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(Long userId, String title, String message) {
        NotificationDto notificationDto = new NotificationDto(null, title, message, null, null);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notificationDto);
    }
}
