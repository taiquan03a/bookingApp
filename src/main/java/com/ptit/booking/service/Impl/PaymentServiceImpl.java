package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.dto.room.RoomRequest;
import com.ptit.booking.dto.serviceRoom.ServiceRoomDto;
import com.ptit.booking.enums.EnumBookingStatus;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.*;
import com.ptit.booking.service.PaymentService;
import com.ptit.booking.service.ZaloPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Service
@RequiredArgsConstructor

public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ZaloPayService zaloPayService;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final BookingRoomRepository bookingRoomRepository;

    @Override
    @Transactional()
    public ResponseEntity<?> checkout(BookingRoomRequest bookingRoomRequest, Principal principal) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }

        Hotel hotel = hotelRepository.findById(bookingRoomRequest.getHotelId())
                .orElseThrow(()-> new AppException(ErrorCode.HOTEL_NOTFOUND));

        Booking booking = Booking.builder()
                .user(user)
                .hotel(hotel)
                .checkIn(bookingRoomRequest.getCheckInDate())
                .checkOut(bookingRoomRequest.getCheckOutDate())
                .status(EnumBookingStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .roomCount(bookingRoomRequest.getRoomRequestList().size())
                .totalPrice(BigDecimal.ZERO)
                .promotionPrice(BigDecimal.ZERO)
                .couponPrice(BigDecimal.ZERO)
                .totalServicePrice(BigDecimal.ZERO)
                .build();
        bookingRepository.save(booking);

        List<BookingRoom> bookingRoomList = new ArrayList<>();

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalServicePrice = BigDecimal.ZERO;

        for(RoomRequest roomRequest : bookingRoomRequest.getRoomRequestList()){
            Room room = roomRepository.findById(roomRequest.getRoomId())
                    .orElseThrow(()->new AppException(ErrorCode.ROOM_NOT_FOUND));

            BigDecimal priceServiceRoom = BigDecimal.ZERO;
            BigDecimal priceRoom = BigDecimal.valueOf(roomRequest.getPrice());
            for(ServiceRoomDto serviceRoomDto : roomRequest.getServiceList()){

            }
            BookingRoom bookingRoom = BookingRoom.builder()
                    .room(room)
                    .booking(booking)
                    .adults(roomRequest.getAdults())
                    .children(roomRequest.getChildren())
                    .priceRoom(BigDecimal.valueOf(roomRequest.getPrice()))
                    .build();
            bookingRoomList.add(bookingRoom);
        }
        bookingRoomRepository.saveAll(bookingRoomList);
        return null;
    }
}
