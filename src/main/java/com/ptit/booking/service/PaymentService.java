package com.ptit.booking.service;

import com.ptit.booking.dto.booking.BookingRoomRequest;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface PaymentService {
    ResponseEntity<?> checkout (BookingRoomRequest bookingRoomRequest, Principal principal);
}
