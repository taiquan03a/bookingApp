package com.ptit.booking.dto.hotelDetail;

import com.ptit.booking.dto.policy.PolicyRoom;
import com.ptit.booking.model.Service;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoomResponse {
    private Long roomId;
    private String roomName;
    private int area;
    private String bed;
    private Set<Service> serviceList;
    private int selectDay;
    private float price;
    private int roomQuantity;
    private Set<PolicyRoom> policyRoomList;
}
