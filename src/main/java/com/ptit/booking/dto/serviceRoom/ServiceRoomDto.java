package com.ptit.booking.dto.serviceRoom;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ServiceRoomDto {
    private Long id;
    private Long quantity;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss",timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime time;
    private String note;
}
