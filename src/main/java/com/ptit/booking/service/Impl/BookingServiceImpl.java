package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.booking.BookingDetail;
import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.dto.room.RoomBooked;
import com.ptit.booking.dto.room.RoomRequest;
import com.ptit.booking.dto.serviceRoom.ServiceBooked;
import com.ptit.booking.dto.serviceRoom.ServiceRoomDto;
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

            List<ServiceBooked> serviceBookedList = new ArrayList<>();
            float priceService = 0;
            for(ServiceRoomDto service: roomRequest.getServiceList()){
                ServiceEntity serviceEntity = serviceRepository.findById(service.getServiceId())
                        .orElseThrow(()->new AppException(ErrorCode.SERVICE_NOT_FOUND));
                float priceServiceCurrentRoom = serviceEntity.getPrice().floatValue() * service.getQuantity().floatValue();
                serviceBookedList.add(ServiceBooked.builder()
                                .serviceId(serviceEntity.getId())
                                .serviceType(serviceEntity.getServiceType())
                                .serviceName(serviceEntity.getName())
                                .image(serviceEntity.getImage())
                                .description(serviceEntity.getDescription())
                                .price(String.valueOf(serviceEntity.getPrice()))
                                .quantity(service.getQuantity())
                                .time(service.getTime())
                                .note(service.getNote())
                                .priceBooked(String.valueOf(priceServiceCurrentRoom))
                        .build());
                priceService += priceServiceCurrentRoom;
            }

            RoomBooked roomBooked = RoomBooked.builder()
                    .roomId(roomSelect.getId())
                    .roomName(roomSelect.getName())
                    .adults(roomRequest.getAdults())
                    .serviceSelect(serviceBookedList)
                    .policyBooked(policyByHotelList)
                    .priceRoom(roomRequest.getPrice())
                    .priceService(priceService)
                    .build();
            totalPriceRoom += roomBooked.getPriceRoom();
            totalPriceService += priceService;
            totalAdults += roomBooked.getAdults();
            roomBookedList.add(roomBooked);
        }
        long selectDay = ChronoUnit.DAYS.between(bookingRoomRequest.getCheckInDate(), bookingRoomRequest.getCheckOutDate());
        float totalPrice = selectDay * (totalPriceRoom + totalPriceService);
        float priceCoupon = 0;
        Coupon coupon = new Coupon();
        if(bookingRoomRequest.getCouponId() == 0){
            List<Coupon> couponList = couponRepository
                    .findBestCouponByUser(
                            "",
                            user,
                            BigDecimal.valueOf(totalPrice)
                    );
            if(!couponList.isEmpty()){
                coupon = couponList.get(0);
                priceCoupon = couponRepository.calculateDiscountAmount(
                        coupon,BigDecimal.valueOf(totalPrice)).floatValue();
            }
        }else{
            coupon = couponRepository.findById(bookingRoomRequest.getCouponId())
                    .orElseThrow(()-> new AppException(ErrorCode.COUPON_NOT_FOUND));
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
