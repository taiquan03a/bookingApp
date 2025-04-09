package com.ptit.booking.dto.serviceRoom;

import lombok.Data;

@Data
public class RoomSelectRequest {
    private Long roomId;
    private Long quantity;
}
