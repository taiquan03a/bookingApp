package com.ptit.booking.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/hotel")
public class HotelController {
    private final HotelService hotelService;

    @GetMapping("/home")
    public ResponseEntity<?> home(Principal principal) {
        return hotelService.home(principal);
    }
    @GetMapping("/filter")
    public ResponseEntity<?> filter(
            @RequestParam(value = "sortBy",required = false,defaultValue = "id") String sortBy,
            @RequestParam(value = "sort",required = false,defaultValue = "asc") String sort,
            @RequestParam(value = "page",required = false,defaultValue = "0") int page,
            @RequestBody FilterRequest filterRequest,
            Principal principal
            ) throws JsonProcessingException {
        return hotelService.search(sortBy,sort,page,filterRequest,principal);
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
