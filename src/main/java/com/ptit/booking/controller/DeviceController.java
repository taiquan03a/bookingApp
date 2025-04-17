package com.ptit.booking.controller;

import com.ptit.booking.dto.notification.DeviceRegistrationRequest;
import com.ptit.booking.repository.UserRepository;
import com.ptit.booking.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerDevice(@Valid @RequestBody DeviceRegistrationRequest request, Principal principal) {
        deviceService.registerDevice(request,principal);
        return ResponseEntity.ok().build();
    }
}

//record DeviceRegistrationRequest(Long userId, String deviceToken, EnumDeviceType deviceType) {}