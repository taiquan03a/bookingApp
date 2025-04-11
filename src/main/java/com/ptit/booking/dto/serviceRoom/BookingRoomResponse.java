package com.ptit.booking.dto.serviceRoom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingRoomResponse {
    private Long roomId;
    private String roomName;
    private Long quantity;
}
