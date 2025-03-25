package com.ptit.booking.controller;

import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/hotel")
public class HotelController {
    private final HotelService hotelService;
    @RequestMapping("/home")
    public ResponseEntity<?> home() {
        return hotelService.home();
    }
    @RequestMapping("/filter")
    public ResponseEntity<?> filter(
            @RequestParam(value = "sortBy",required = false,defaultValue = "id") String sortBy,
            @RequestParam(value = "sort",required = false,defaultValue = "asc") String sort,
            @RequestParam(value = "page",required = false,defaultValue = "0") int page,
            @RequestBody FilterRequest filterRequest
            ){
        return hotelService.search(sortBy,sort,page,filterRequest);
    }
}
