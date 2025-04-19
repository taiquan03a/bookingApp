package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.hotelDetail.RoomResponse;
import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.dto.policy.PolicyRoom;
import com.ptit.booking.dto.promotion.PromotionBookingRoom;
import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.enums.EnumPromotionType;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.HotelRepository;
import com.ptit.booking.repository.RoomRepository;
import com.ptit.booking.repository.ServiceRepository;
import com.ptit.booking.service.RoomService;
import com.ptit.booking.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.ptit.booking.constants.ErrorMessage.CHECKIN_AFTER_CHECKOUT;
import static com.ptit.booking.constants.ErrorMessage.CHECKIN_MUST_TODAY_OR_FUTURE;

@Service
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
                                        room,selectRoomRequest.getCheckInDate().atTime(hotel.getCheckInTime()),
                                        selectRoomRequest.getCheckOutDate().atTime(hotel.getCheckOutTime())
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

    @Override
    public ResponseEntity<?> bookingRooms(BookingRoomRequest bookingRoomRequest, Principal principal) {

        return null;
    }


    private RoomResponse mapRoomResponse(Room room, int selectDay,int availableRoom) {
        Set<ServiceEntity> serviceEntitySet = serviceRepository.findAllByRoom(room);
        Set<PolicyRoom> policyRoomList = new LinkedHashSet<>();
        for(HotelPolicy hotelPolicy:room.getHotel().getHotelPolicies()){
            PolicyRoom policyRoom = PolicyRoom.builder()
                    .policyId(hotelPolicy.getPolicy().getId())
                    .policyDescription(hotelPolicy.getPolicy().getDescription())
                    .policyName(hotelPolicy.getPolicy().getType())
                    .build();
            policyRoomList.add(policyRoom);
        }
        List<Promotion> promotionList = room.getHotel().getPromotions()
                .stream()
                .filter(Promotion::getStatus)
                .filter(promotion -> LocalDateTime.now().isAfter(promotion.getStartDate()))
                .filter(promotion -> LocalDateTime.now().isBefore(promotion.getEndDate()))
                .toList();
        float originalPrice = room.getPrice().floatValue();
        float promotionPrice = promotionList.stream()
                .map(promotion -> {
                   if(promotion.getDiscountType().equals(EnumPromotionType.PERCENTAGE.name())){
                       float discountPercentage = Float.parseFloat(promotion.getDiscountValue().replace("%", ""));
                       return originalPrice * (discountPercentage / 100.0f);
                   }else{
                        return Float.parseFloat(promotion.getDiscountValue());
                   }
                })
                .reduce(0.0f, Float::sum);
        float finalPrice = (originalPrice - promotionPrice) * selectDay;
        PromotionBookingRoom promotionBookingRoom = PromotionBookingRoom.builder()
                .id(promotionList.stream().findFirst().get().getId())
                .discountValue(promotionList.stream().findFirst().get().getDiscountValue())
                .name(promotionList.stream().findFirst().get().getName())
                .build();
        return RoomResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .area(room.getArea())
                .bed(room.getBed())
                .serviceEntityList(serviceEntitySet)
                .selectDay(selectDay)
                .promotionPrice(originalPrice * selectDay)
                .price(finalPrice)
                .promotion(promotionBookingRoom)
                .roomQuantity(availableRoom)
                .policyRoomList(policyRoomList)
                .build();
    }
}
