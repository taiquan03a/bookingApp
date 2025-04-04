package com.ptit.booking.service;

import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import org.springframework.http.ResponseEntity;

public interface RoomService {
    ResponseEntity<?> selectRooms (SelectRoomRequest selectRoomRequest);
    //ResponseEntity<?> bookingRooms ();
}
