package com.ptit.booking.dto.serviceRoom;

import lombok.Data;
import java.util.List;

@Data
public class ServiceBookingRequest {
    private Long serviceId;
    private List<RoomBookingRequest> roomBookingRequestList;

}

