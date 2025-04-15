package com.ptit.booking.dto.serviceRoom;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingRoomResponse {
    private Long roomId;
    private String roomName;
    private Long quantity;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime time;
    private String note;
}
