package com.ptit.booking.mapping;

import com.ptit.booking.dto.hotelDetail.RoomResponse;
import com.ptit.booking.dto.policy.PolicyRoom;
import com.ptit.booking.enums.EnumServiceType;
import com.ptit.booking.model.HotelPolicy;
import com.ptit.booking.model.Room;
import com.ptit.booking.model.Service;
import com.ptit.booking.model.ServiceRoom;

import java.util.*;
import java.util.stream.Collectors;

public class RoomResponseMapper {
    public RoomResponse mapRoomResponse(Room room, int selectDay,int availableRoom) {
        Set<Service> serviceRoomList = room.getServiceRooms()
                .stream()
                .map(ServiceRoom::getService)
                .filter(service -> service.getServiceType().equals(String.valueOf(EnumServiceType.AMENITY)))
                .collect(Collectors.toSet());
        Set<PolicyRoom> policyRoomList = new LinkedHashSet<>();
        for(HotelPolicy hotelPolicy:room.getHotel().getHotelPolicies()){
            PolicyRoom policyRoom = PolicyRoom.builder()
                    .policyId(hotelPolicy.getPolicy().getId())
                    .policyDescription(hotelPolicy.getDescription())
                    .policyName(hotelPolicy.getPolicy().getName())
                    .build();
            policyRoomList.add(policyRoom);
        }
        return RoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .area(room.getArea())
                .bed(room.getBed())
                .serviceList(serviceRoomList)
                .selectDay(selectDay)
                .price(selectDay * room.getPrice().floatValue())
                .roomQuantity(availableRoom)
                .policyRoomList(policyRoomList)
                .build();
    }
}
