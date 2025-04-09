package com.ptit.booking.dto.serviceRoom;

import lombok.Data;

@Data
public class RoomBookingRequest {
    private Long roomId;
    private Long quantity;
}
