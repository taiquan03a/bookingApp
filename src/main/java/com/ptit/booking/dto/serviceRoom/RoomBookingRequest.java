package com.ptit.booking.dto.serviceRoom;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RoomBookingRequest {
    @Schema(description = "id phòng", example = "1", defaultValue = "1")
    private Long roomId;
    @Schema(description = "Số lượng phòng", example = "3", defaultValue = "1")
    private Long quantity;
}
