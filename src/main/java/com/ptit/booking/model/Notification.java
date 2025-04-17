package com.ptit.booking.model;

import com.ptit.booking.enums.EnumNotificationStatus;
import com.ptit.booking.enums.EnumNotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    @ColumnDefault("0")
    @Column(name = "is_read")
    private Boolean isRead;

    @ColumnDefault("(now())")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Size(max = 50)
    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EnumNotificationStatus status;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private EnumNotificationType type;

    @Column(name = "send_at")
    private LocalDateTime sendAt;

}