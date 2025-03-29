package com.ptit.booking.controller;

import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.service.FoursquareService;
import com.ptit.booking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
    public ResponseEntity<?> hotelDetail(
            @PathVariable Long id,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate
            ){
        return hotelService.hotelDetail(id,checkInDate,checkOutDate);
    }
    @GetMapping("/select_room")
    public ResponseEntity<?> selectRoom(@RequestBody SelectRoomRequest selectRoomRequest){
        return hotelService.selectRooms(selectRoomRequest);
    }

}
