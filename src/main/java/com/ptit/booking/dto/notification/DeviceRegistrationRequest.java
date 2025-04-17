package com.ptit.booking.dto.notification;

import com.google.firebase.database.annotations.NotNull;
import com.ptit.booking.enums.EnumDeviceType;
import jakarta.validation.constraints.NotBlank;

public record DeviceRegistrationRequest(
        @NotBlank(message = "deviceToken cannot be empty")
        String deviceToken,

        @NotNull()
        EnumDeviceType deviceType
) {}