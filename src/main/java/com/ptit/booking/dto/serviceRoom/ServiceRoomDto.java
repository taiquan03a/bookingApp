package com.ptit.booking.dto.serviceRoom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceRoomDto {
    private Long serviceId;
    private Long quantity;
}
