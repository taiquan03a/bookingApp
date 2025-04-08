package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.serviceRoom.ServiceRoomBooking;
import com.ptit.booking.dto.serviceRoom.ServiceRoomSelect;
import com.ptit.booking.enums.EnumServiceType;
import com.ptit.booking.mapping.ServiceMapper;
import com.ptit.booking.model.Room;
import com.ptit.booking.model.ServiceEntity;
import com.ptit.booking.repository.RoomRepository;
import com.ptit.booking.repository.ServiceRepository;
import com.ptit.booking.repository.ServiceRoomRepository;
import com.ptit.booking.service.ServiceEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceEntityServiceImpl implements ServiceEntityService {
    private final ServiceRepository serviceRepository;
    private final RoomRepository roomRepository;
    private final ServiceMapper serviceMapper;

    @Override
    public ResponseEntity<?> getServiceRooms() {
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.FILTER_PAGE)
                .data(serviceRepository.findAll())
                .build());
    }

    @Override
    public ResponseEntity<?> getServiceBookings(List<Long> roomIdList) {
        List<Room> roomList = roomRepository.findAllById(roomIdList);
        List<ServiceRoomBooking> serviceRoomBookingList = new ArrayList<>();
        for(EnumServiceType type : EnumServiceType.values()) {
            if(!type.name().equals(EnumServiceType.AMENITY.name())){
                ServiceRoomBooking serviceRoomBooking = ServiceRoomBooking.builder()
                        .serviceType(type.name())
                        .serviceRoomList(
                                serviceMapper.toDtoList(
                                        serviceRepository.findByRoomsAndType(roomList, type.name())
                                )
                        )
                        .build();
                serviceRoomBookingList.add(serviceRoomBooking);
            }
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.LIST_SERVICE_ROOMS_SUCCESSFULLY)
                .data(serviceRoomBookingList)
                .build());
    }
}
