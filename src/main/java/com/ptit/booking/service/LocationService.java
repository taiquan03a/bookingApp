package com.ptit.booking.service;

import org.springframework.http.ResponseEntity;

public interface LocationService {
    ResponseEntity<?> getAllLocations();
}
