package com.ptit.booking.service.Impl;

import com.ptit.booking.dto.notification.NotificationDto;
import com.ptit.booking.enums.EnumNotificationType;
import com.ptit.booking.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {
    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationServiceImpl.class);
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(Long userId, Long notificationId, String title, String message, EnumNotificationType type,boolean isRead,LocalDateTime createAt) {
        NotificationDto notificationDto = new NotificationDto(notificationId, title, message, type, isRead,createAt);
        log.info("Sending notification to user {}", userId);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notificationDto);
    }
}
