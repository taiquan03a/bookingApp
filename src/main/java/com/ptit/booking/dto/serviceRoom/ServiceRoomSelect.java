package com.ptit.booking.dto.serviceRoom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceRoomSelect {
    private Long id;
    private String name;
    private String description;
    private String image;
    private String price;
}
