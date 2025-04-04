package com.ptit.booking.controller;

import com.ptit.booking.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("get_list")
    public ResponseEntity<?> getList(){
        return locationService.getAllLocations();
    }
}
