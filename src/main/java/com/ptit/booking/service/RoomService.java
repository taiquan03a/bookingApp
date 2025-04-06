package com.ptit.booking.service;

import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.dto.room.BookingRoomRequest;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface RoomService {
    ResponseEntity<?> selectRooms (SelectRoomRequest selectRoomRequest);
    ResponseEntity<?> bookingRooms (BookingRoomRequest bookingRoomRequest, Principal principal);
}
