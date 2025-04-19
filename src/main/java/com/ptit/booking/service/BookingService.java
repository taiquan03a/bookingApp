package com.ptit.booking.service;

import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.dto.booking.CancelBookingRequest;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface BookingService {
    ResponseEntity<?> booking(BookingRoomRequest bookingRoomRequest, Principal principal);
    ResponseEntity<?> historyBooking(Principal principal);
    ResponseEntity<?> cancelBooking(CancelBookingRequest cancelBookingRequest, Principal principal) throws Exception;
}
