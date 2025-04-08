package com.ptit.booking.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "home",description = "api hiển thị danh sách các khách sạn ở trang chủ")
    public ResponseEntity<?> home(Principal principal) {
        return hotelService.home(principal);
    }
    @PostMapping("/filter")
    @Operation(summary = "search hotel",description = "api tìm kiếm khách sạn.")
    public ResponseEntity<?> filter(
            @RequestParam(value = "sortBy",required = false,defaultValue = "price") String sortBy,
            @RequestParam(value = "sort",required = false,defaultValue = "asc") String sort,
            @RequestParam(value = "page",required = false,defaultValue = "0") int page,
            @RequestBody FilterRequest filterRequest,
            Principal principal
            ) throws JsonProcessingException {
        return hotelService.search(sortBy,sort,page,filterRequest,principal);
    }
    @GetMapping("/hotel_detail/{id}")
    @Operation(summary = "hotel detail",description = "api hiển thị thông tin chi tiết của khách sạn.")
    public ResponseEntity<?> hotelDetail(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate checkInDate,
            @RequestParam(required = false) LocalDate checkOutDate
            ){
        if (checkInDate == null) {
            checkInDate = LocalDate.now();
        }
        if (checkOutDate == null) {
            checkOutDate = checkInDate.plusDays(1);
        }
        return hotelService.hotelDetail(id,checkInDate,checkOutDate);
    }
}
