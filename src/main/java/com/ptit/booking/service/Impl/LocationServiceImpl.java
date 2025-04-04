package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.repository.LocationRepository;
import com.ptit.booking.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Override
    public ResponseEntity<?> getAllLocations() {
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.LOCATION_PAGE)
                .data(locationRepository.findAll())
                .build());
    }
}
