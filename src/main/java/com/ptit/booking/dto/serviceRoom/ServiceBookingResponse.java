package com.ptit.booking.dto.serviceRoom;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ServiceBookingResponse {
    private Long serviceId;
    private String serviceName;
    List<BookingRoomResponse> bookingRoomResponseList;
}
