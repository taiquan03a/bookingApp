package com.ptit.booking.controller;

import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.dto.booking.CancelBookingRequest;
import com.ptit.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/booking")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("get_booking")
    @Operation(summary = "get booking detail",description = "api hiển thị chi tiết về đơn đặt")
    public ResponseEntity<?> getBooking(@RequestBody BookingRoomRequest bookingRoomRequest, Principal principal) {
        return bookingService.booking(bookingRoomRequest, principal);
    }

    @GetMapping("history_booking")
    public ResponseEntity<?> getBookingHistory(Principal principal) {
        return bookingService.historyBooking(principal);
    }

    @PostMapping("cancel")
    public ResponseEntity<?> cancelBooking(@RequestBody CancelBookingRequest cancelBookingRequest, Principal principal) throws Exception {
        return bookingService.cancelBooking(cancelBookingRequest,principal);
    }
}
