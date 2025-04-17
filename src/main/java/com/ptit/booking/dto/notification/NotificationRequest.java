package com.ptit.booking.dto.notification;

import com.ptit.booking.enums.EnumNotificationType;

public record NotificationRequest(
        Long userId,
        String title,
        String message,
        EnumNotificationType type
) {}
