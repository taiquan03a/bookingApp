package com.ptit.booking.dto.serviceRoom;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ServiceRoomBooking {
    private String serviceType;
    List<ServiceRoomSelect> serviceRoomList;
}
