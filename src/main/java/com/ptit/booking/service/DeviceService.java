package com.ptit.booking.service;

import com.ptit.booking.dto.notification.DeviceRegistrationRequest;

import java.security.Principal;

public interface DeviceService {
    void registerDevice(DeviceRegistrationRequest request, Principal principal);
}
