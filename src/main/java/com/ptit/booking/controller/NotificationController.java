package com.ptit.booking.controller;

import com.ptit.booking.dto.notification.NotificationDto;
import com.ptit.booking.dto.notification.NotificationRequest;
import com.ptit.booking.enums.EnumNotificationType;
import com.ptit.booking.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.sendNotification(
                request.userId(),
                request.title(),
                request.message(),
                request.type()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) {
        Page<NotificationDto> notifications = notificationService.getUserNotifications(principal, PageRequest.of(page, size))
                .map(NotificationDto::fromEntity);
        return ResponseEntity.ok(notifications.getContent());
    }
    @GetMapping("/{notiId}/read")
    public boolean readNotification(@PathVariable long notiId,Principal principal) {
        return notificationService.readNotification(notiId);
    }
}
