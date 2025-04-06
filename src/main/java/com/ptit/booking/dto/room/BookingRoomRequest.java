package com.ptit.booking.dto.room;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRoomRequest {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    List<RoomRequest> roomRequestList;
}
