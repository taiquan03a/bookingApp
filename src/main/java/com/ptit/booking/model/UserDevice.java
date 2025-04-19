package com.ptit.booking.model;

import com.ptit.booking.enums.EnumDeviceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_devices")
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Size(max = 255)
    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    private EnumDeviceType deviceType;

    @Column(name = "create_at")
    private LocalDateTime createAt = LocalDateTime.now();

}