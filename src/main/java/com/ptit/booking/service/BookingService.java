package com.ptit.booking.service;

import com.ptit.booking.dto.room.BookingRoomRequest;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface BookingService {
    ResponseEntity<?> booking(BookingRoomRequest bookingRoomRequest, Principal principal);

}
