package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.room.RoomChoseService;
import com.ptit.booking.dto.serviceRoom.RoomSelectRequest;
import com.ptit.booking.dto.serviceRoom.ServiceBookingRequest;
import com.ptit.booking.dto.serviceRoom.ServiceRoomBooking;
import com.ptit.booking.dto.serviceRoom.ServiceRoomSelect;
import com.ptit.booking.enums.EnumServiceType;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.mapping.ServiceMapper;
import com.ptit.booking.model.Room;
import com.ptit.booking.model.ServiceEntity;
import com.ptit.booking.model.ServiceRoom;
import com.ptit.booking.repository.RoomRepository;
import com.ptit.booking.repository.ServiceRepository;
import com.ptit.booking.repository.ServiceRoomRepository;
import com.ptit.booking.service.ServiceEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> getServiceBookings(List<RoomSelectRequest> roomSelectRequestList) {
        List<Room> roomList = roomRepository.findAllById(
                roomSelectRequestList
                        .stream()
                        .map(RoomSelectRequest::getRoomId)
                        .collect(Collectors.toList())
        );
        List<ServiceRoomBooking> serviceRoomBookingList = new ArrayList<>();
        for(EnumServiceType type : EnumServiceType.values()) {
            if(!type.name().equals(EnumServiceType.AMENITY.name())){
                ServiceRoomBooking serviceRoomBooking = ServiceRoomBooking.builder()
                        .serviceType(type.name())
                        .serviceRoomList(
                                mapToDtoList(
                                        serviceRepository.findByRoomsAndType(roomList, type.name()),
                                        roomSelectRequestList
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

    @Override
    public ResponseEntity<?> getServiceDetail(Long serviceId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.SERVICE_ROOMS_SUCCESSFULLY)
                .data(serviceRepository.findById(serviceId).orElseThrow(()-> new AppException(ErrorCode.SERVICE_NOT_FOUND)))
                .build());
    }

    @Override
    public ResponseEntity<?> getCartService(List<ServiceBookingRequest> serviceBookingRequestList) {

        return null;
    }

    private static List<ServiceRoomSelect> mapToDtoList(List<ServiceEntity> entities, List<RoomSelectRequest> inputRoomList) {
        return entities.stream()
                .map(entity -> mapToDto(entity, inputRoomList))
                .collect(Collectors.toList());
    }

    private static ServiceRoomSelect mapToDto(ServiceEntity entity, List<RoomSelectRequest> inputRoomList) {
        Map<Long, Long> roomQuantityMap = inputRoomList.stream()
                .collect(Collectors.toMap(RoomSelectRequest::getRoomId, RoomSelectRequest::getQuantity));

        List<RoomChoseService> repeatedRooms = entity.getServiceRooms()
                .stream()
                .map(ServiceRoom::getRoom)
                .filter(room -> roomQuantityMap.containsKey(room.getId()))
                .flatMap(room -> {
                    Long quantity = roomQuantityMap.get(room.getId());
                    return Collections.nCopies(quantity.intValue(),
                            new RoomChoseService(room.getId(), room.getName())
                    ).stream();
                })
                .collect(Collectors.toList());

        return ServiceRoomSelect.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .image(entity.getImage())
                .price(formatPrice(entity.getPrice()))
                .roomChoseServiceList(repeatedRooms)
                .build();
    }


    private static String formatPrice(BigDecimal price) {
        if (price == null) return "0.00";
        return new DecimalFormat("#,###.00").format(price);
    }
}
