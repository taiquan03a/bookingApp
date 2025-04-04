package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.hotelDetail.RoomResponse;
import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.dto.policy.PolicyRoom;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.HotelPolicy;
import com.ptit.booking.model.Room;
import com.ptit.booking.model.ServiceEntity;
import com.ptit.booking.repository.HotelRepository;
import com.ptit.booking.repository.RoomRepository;
import com.ptit.booking.repository.ServiceRepository;
import com.ptit.booking.service.RoomService;
import com.ptit.booking.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.ptit.booking.constants.ErrorMessage.CHECKIN_AFTER_CHECKOUT;
import static com.ptit.booking.constants.ErrorMessage.CHECKIN_MUST_TODAY_OR_FUTURE;

@org.springframework.stereotype.Service
@Transactional
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public ResponseEntity<?> selectRooms(SelectRoomRequest selectRoomRequest) {
        long daysBetween = ChronoUnit.DAYS.between(selectRoomRequest.getCheckInDate(), selectRoomRequest.getCheckOutDate());
        if(selectRoomRequest.getCheckInDate().isBefore(LocalDate.now())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(402)
                    .message("CHECKIN_MUST_TODAY_OR_FUTURE")
                    .description(CHECKIN_MUST_TODAY_OR_FUTURE)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        if(daysBetween < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(403)
                    .message("CHECKIN_AFTER_CHECKOUT")
                    .description(CHECKIN_AFTER_CHECKOUT)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }

        Hotel hotel = hotelRepository.findById(selectRoomRequest.getHotelId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Specification<Room> specRoomAvi = RoomSpecification.availableRooms(selectRoomRequest,hotel);
        List<RoomResponse> re = roomRepository.findAll(specRoomAvi)
                .stream()
                .map(
                        room -> mapRoomResponse(
                                room,
                                (int) ChronoUnit.DAYS.between(
                                        selectRoomRequest.getCheckInDate(),
                                        selectRoomRequest.getCheckOutDate()
                                ),
                                roomRepository.countAvailableRoom(
                                        room,selectRoomRequest.getCheckInDate(),
                                        selectRoomRequest.getCheckOutDate()
                                )
                        )
                )
                .toList();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.LIST_ROOM_AVAILABLE + selectRoomRequest.getHotelId())
                .data(re)
                .build());
    }


    private RoomResponse mapRoomResponse(Room room, int selectDay,int availableRoom) {
        Set<ServiceEntity> serviceEntitySet = serviceRepository.findAllByRoom(room);
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
                .serviceEntityList(serviceEntitySet)
                .selectDay(selectDay)
                .price(selectDay * room.getPrice().floatValue())
                .roomQuantity(availableRoom)
                .policyRoomList(policyRoomList)
                .build();
    }
}
