package com.ptit.booking.dto.serviceRoom;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class RoomBookingRequest {
    private Long roomId;
    private Long quantity;
}
