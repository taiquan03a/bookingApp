package com.ptit.booking.dto.booking;

import com.ptit.booking.dto.room.RoomRequest;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRoomRequest {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    List<RoomRequest> roomRequestList;

}
