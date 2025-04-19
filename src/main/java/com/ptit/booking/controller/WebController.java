package com.ptit.booking.controller;

import com.ptit.booking.dto.web.HotelWebResponse;
import com.ptit.booking.service.WebService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("api/web")
@RequiredArgsConstructor
public class WebController {
    private final WebService webService;

    @GetMapping("hotel_list")
    public ResponseEntity<?> hotelList() {
        return webService.getAllHotel();
    }

    @GetMapping("booking/{hotelId}")
    public ResponseEntity<?> booking(@PathVariable Long hotelId) {
        return webService.getBookingByHotel(hotelId);
    }

    @GetMapping("booking_detail/{bookingId}")
    public ResponseEntity<?> bookingDetail(@PathVariable Long bookingId) {
        return webService.getBookingDetail(bookingId);
    }
}
