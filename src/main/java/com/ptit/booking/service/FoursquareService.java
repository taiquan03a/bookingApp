package com.ptit.booking.service;

import org.springframework.http.ResponseEntity;

public interface FoursquareService {
    ResponseEntity<?> searchPlaces(String location);
}
