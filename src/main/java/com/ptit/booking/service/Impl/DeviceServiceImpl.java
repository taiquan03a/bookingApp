package com.ptit.booking.service.Impl;

import com.ptit.booking.UserDevice;
import com.ptit.booking.dto.notification.DeviceRegistrationRequest;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.model.User;
import com.ptit.booking.repository.UserDeviceRepository;
import com.ptit.booking.repository.UserRepository;
import com.ptit.booking.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void registerDevice(DeviceRegistrationRequest request, Principal principal) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        UserDevice device = UserDevice.builder()
                .user(user)
                .deviceToken(request.deviceToken())
                .deviceType(request.deviceType())
                .build();
        userDeviceRepository.save(device);

    }
}
