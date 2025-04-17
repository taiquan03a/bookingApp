package com.ptit.booking.dto.notification;

import com.ptit.booking.enums.EnumNotificationType;
import com.ptit.booking.model.Notification;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String title,
        String message,
        EnumNotificationType type,
        LocalDateTime createdAt
) {
    public static NotificationDto fromEntity(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getCreatedAt()
        );
    }
}
