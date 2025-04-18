package com.ptit.booking.dto.room;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ptit.booking.dto.serviceRoom.ServiceBooked;
import com.ptit.booking.model.Policy;
import com.ptit.booking.model.ServiceEntity;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomBooked {
    private Long roomId;
    private String roomName;
    private int adults;
    private float priceRoom;
    private float priceService;
    private List<ServiceBooked> serviceSelect;
    private List<Policy> policyBooked;
}
