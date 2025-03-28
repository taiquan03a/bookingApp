package com.ptit.booking.controller;

import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.service.FoursquareService;
import com.ptit.booking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/hotel")
public class HotelController {
    private final HotelService hotelService;

    @GetMapping("/home")
    public ResponseEntity<?> home() {
        return hotelService.home();
    }
    @GetMapping("/filter")
    public ResponseEntity<?> filter(
            @RequestParam(value = "sortBy",required = false,defaultValue = "id") String sortBy,
            @RequestParam(value = "sort",required = false,defaultValue = "asc") String sort,
            @RequestParam(value = "page",required = false,defaultValue = "0") int page,
            @RequestBody FilterRequest filterRequest
            ){
        return hotelService.search(sortBy,sort,page,filterRequest);
    }
    @GetMapping("/hotel_detail/{id}")
    public ResponseEntity<?> hotelDetail(@PathVariable Long id){
        return hotelService.hotelDetail(id);
    }

}
