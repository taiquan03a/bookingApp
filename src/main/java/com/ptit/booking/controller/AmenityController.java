package com.ptit.booking.controller;

import com.ptit.booking.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("amenity")
public class AmenityController {
    private final AmenityService amenityService;
    @GetMapping("get_list")
    public ResponseEntity<?> getList(){
        return amenityService.getAmenities();
    }
}
