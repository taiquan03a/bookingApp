package com.ptit.booking.dto.serviceRoom;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomBookingRequest {
    @Schema(description = "id phòng", example = "1", defaultValue = "1")
    private Long roomId;
    @Schema(description = "Số lượng phòng", example = "3", defaultValue = "1")
    private Long quantity;
    @Schema(description = "thời gian đặt phục vụ dịnh vụ",defaultValue = "2025-04-15 18:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime time;
    private String note;
}
