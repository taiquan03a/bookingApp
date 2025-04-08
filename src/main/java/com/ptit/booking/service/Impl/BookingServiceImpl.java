package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.booking.BookingDetail;
import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.dto.room.RoomBooked;
import com.ptit.booking.dto.room.RoomRequest;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.*;
import com.ptit.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final RoomRepository roomRepository;
    private final CouponRepository couponRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final HotelRepository hotelRepository;

    @Override
    public ResponseEntity<?> booking(BookingRoomRequest bookingRoomRequest, Principal principal) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        System.out.println("booking hotel id -> "+bookingRoomRequest.getHotelId());
        Hotel hotel = hotelRepository.findHotelById(bookingRoomRequest.getHotelId())
                .orElseThrow(()-> new AppException(ErrorCode.HOTEL_NOT_FOUND));
        List<RoomBooked> roomBookedList = new ArrayList<>();
        List<Policy> policyByHotelList = hotelRepository.findPoliciesByHotel(hotel);
        float totalPriceRoom = 0;
        float totalPriceService = 0;
        int totalAdults = 0;
        for(RoomRequest roomRequest: bookingRoomRequest.getRoomRequestList()){
            Room roomSelect = roomRepository.findById(roomRequest.getRoomId())
                    .orElseThrow(()-> new AppException(ErrorCode.ROOM_NOT_FOUND));
            if(!roomRepository.existsRoomByHotel(hotel))
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(
                                ErrorResponse.builder()
                                        .statusCode(407)
                                        .message(ErrorMessage.ROOM_NOT_IN_HOTEL(hotel.getName()))
                                        .timestamp(new Date(System.currentTimeMillis()))
                                        .build()
                        );
            List<ServiceEntity> serviceEntityList =
                    Optional.ofNullable(roomRequest.getServiceIdList())
                            .filter(list -> !list.isEmpty())
                            .map(serviceRepository::findAllById)
                            .orElse(Collections.emptyList());

            RoomBooked roomBooked = RoomBooked.builder()
                    .roomId(roomSelect.getId())
                    .roomName(roomSelect.getName())
                    .adults(roomRequest.getAdults())
                    .serviceSelect(serviceEntityList)
                    .policyBooked(policyByHotelList)
                    .priceRoom(roomRequest.getPrice())
                    .priceService(
                            serviceEntityList.stream()
                                    .map(ServiceEntity::getPrice)
                                    .filter(Objects::nonNull) // bỏ các giá trị null
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .floatValue()
                    )
                    .build();
            totalPriceRoom += roomBooked.getPriceRoom();
            totalPriceService += roomBooked.getPriceService();
            totalAdults += roomBooked.getAdults();
            roomBookedList.add(roomBooked);
        }
        long selectDay = ChronoUnit.DAYS.between(bookingRoomRequest.getCheckInDate(), bookingRoomRequest.getCheckOutDate());
        float totalPrice = selectDay * (totalPriceRoom + totalPriceService);
        List<Coupon> couponList = couponRepository
                .findBestCouponByUser(
                        user,
                        BigDecimal.valueOf(totalPrice)
                );
        float priceCoupon = 0;
        Coupon coupon = new Coupon();
        if(!couponList.isEmpty()){
            coupon = couponList.get(0);
            priceCoupon = couponRepository.calculateDiscountAmount(
                    coupon,BigDecimal.valueOf(totalPrice)).floatValue();
        }

        BookingDetail bookingDetail = BookingDetail.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelAddress(hotel.getLocation().getName())
                .totalAdults(totalAdults)
                .checkIn(bookingRoomRequest.getCheckInDate().atTime(hotel.getCheckInTime()))
                .checkOut(bookingRoomRequest.getCheckOutDate().atTime(hotel.getCheckOutTime()))
                .roomBookedList(roomBookedList)
                .couponCode(coupon.getCode())
                .priceCoupon(String.valueOf(priceCoupon))
                .totalPriceRoom(String.valueOf(totalPriceRoom))
                .totalPriceService(String.valueOf(totalPriceService))
                .finalPrice(String.valueOf(totalPrice - priceCoupon))
                .build();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.BOOKING_DETAIL_SUCCESSFULLY)
                .data(bookingDetail)
                .build());
    }
}
