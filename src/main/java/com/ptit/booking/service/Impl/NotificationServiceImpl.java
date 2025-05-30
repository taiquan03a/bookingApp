package com.ptit.booking.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.booking.model.UserDevice;
import com.ptit.booking.enums.EnumNotificationStatus;
import com.ptit.booking.enums.EnumNotificationType;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.model.Notification;
import com.ptit.booking.model.User;
import com.ptit.booking.repository.NotificationRepository;
import com.ptit.booking.repository.UserDeviceRepository;
import com.ptit.booking.repository.UserRepository;
import com.ptit.booking.service.NotificationService;
import com.ptit.booking.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Transactional
    public Notification createNotification(Long userId, String title, String message, EnumNotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .status(EnumNotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);
        logger.info("Created notification with ID: {} for userId: {}", notification.getId(), userId);
        return notification;
    }

    @Async
    @Override
    public void sendNotification(Long userId, String title, String message, EnumNotificationType type) {
        try {
            // Tạo và lưu notification trong transaction
            Notification notification = createNotification(userId, title, message, type);

            // Gửi qua WebSocket
            webSocketNotificationService.sendNotificationToUser(userId,notification.getId(), title, message,type,notification.getIsRead(),notification.getCreatedAt());

            // Gửi qua Expo Push
            List<UserDevice> devices = userDeviceRepository.findByUserId(userId);
            if (devices.isEmpty()) {
                logger.warn("No devices found for userId: {}", userId);
                updateNotificationStatus(notification.getId(), EnumNotificationStatus.FAILED);
                return;
            }

            devices.forEach(device -> sendPushNotification(device, title, message, type ,notification.getIsRead(),notification.getCreatedAt(),notification.getId()));
        } catch (Exception e) {
            logger.error("Failed to send notification for userId: {}. Error: {}", userId, e.getMessage());
            throw new AppException(ErrorCode.NOTIFICATION_FAILED);
        }

        CompletableFuture.completedFuture(null);
    }


    private void sendPushNotification(UserDevice device, String title, String message,EnumNotificationType type,boolean isRead,LocalDateTime creatAt, Long notificationId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedCreatedAt = creatAt.format(formatter);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = Map.of(
                    "type", type.toString(),
                    "created_at", formattedCreatedAt,
                    "isRead",isRead
            );
            Map<String, Object> body = Map.of(
                    "to", device.getDeviceToken(),
                    "title", title,
                    "body", message,
                    "data", data
            );

            String payload = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://exp.host/--/api/v2/push/send"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Successfully sent push notification to device: {}", device.getDeviceToken());
                updateNotificationStatus(notificationId, EnumNotificationStatus.SENT);
            } else {
                logger.warn("Failed to send push notification to device: {}. Status: {}", device.getDeviceToken(), response.statusCode());
                updateNotificationStatus(notificationId, EnumNotificationStatus.FAILED);
            }
        } catch (Exception e) {
            logger.error("Error sending push notification to device: {}. Error: {}", device.getDeviceToken(), e.getMessage());
            updateNotificationStatus(notificationId, EnumNotificationStatus.FAILED);
            throw new AppException(ErrorCode.NOTIFICATION_FAILED);
        }
    }
    private String escapeJson(String text) {
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @Transactional
    public void updateNotificationStatus(Long notificationId, EnumNotificationStatus status) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_BAD));
        notification.setStatus(status);
        if (status == EnumNotificationStatus.SENT) {
            notification.setSendAt(LocalDateTime.now());
        }
        notificationRepository.save(notification);
        logger.info("Updated notification ID: {} with status: {}", notificationId, status);
    }

    @Override
    public Page<Notification> getUserNotifications(Principal principal, Pageable pageable) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        return notificationRepository.findByUserId(user.getId(), pageable);
    }

    @Override
    public boolean readNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_BAD));
        if(!notification.getIsRead()){
            notification.setIsRead(true);
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }
}