package com.ptit.booking.dto.serviceRoom;

import com.ptit.booking.dto.room.RoomChoseService;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ServiceRoomSelect {
    private Long id;
    private String name;
    private String description;
    private String image;
    private String price;
    private List<RoomChoseService> roomChoseServiceList;
}
