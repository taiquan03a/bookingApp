package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.booking.HistoryBookingDetailResponse;
import com.ptit.booking.dto.room.RoomBooked;
import com.ptit.booking.dto.serviceRoom.ServiceBooked;
import com.ptit.booking.dto.web.BookingDetailWebResponse;
import com.ptit.booking.dto.web.BookingHotelWebResponse;
import com.ptit.booking.dto.web.BookingWebResponse;
import com.ptit.booking.dto.web.HotelWebResponse;
import com.ptit.booking.dto.zaloPay.CreateOrderRequest;
import com.ptit.booking.enums.EnumBookingStatus;
import com.ptit.booking.enums.EnumPaymentType;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.BookingRepository;
import com.ptit.booking.repository.BookingServiceEntityRepository;
import com.ptit.booking.repository.HotelRepository;
import com.ptit.booking.repository.ServiceRepository;
import com.ptit.booking.service.WebService;
import com.ptit.booking.service.ZaloPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ptit.booking.constants.ErrorMessage.EMAIL_IN_USE;

@Service
@RequiredArgsConstructor
public class WebServiceImpl implements WebService {

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final BookingServiceEntityRepository bookingServiceEntityRepository;
    private final ZaloPayService zaloPayService;

    @Override
    public ResponseEntity<?> getAllHotel() {
        List<Hotel> hotelList = hotelRepository.findAll();
        List<HotelWebResponse> hotelWebResponseList = hotelList.stream()
                .map(hotel -> {
                    return HotelWebResponse.builder()
                            .hotelId(hotel.getId())
                            .hotelName(hotel.getName())
                            .hotelAddress(hotel.getLocation().getName())
                            .build();
                }).toList();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.HOME_PAGE)
                .data(hotelWebResponseList)
                .build());
    }

    @Override
    public ResponseEntity<?> getBookingByHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new AppException(ErrorCode.HOTEL_NOTFOUND));
        List<BookingHotelWebResponse> bookingList = new ArrayList<>();
        for(EnumBookingStatus status : EnumBookingStatus.values()) {
            if(!status.name().equals(EnumBookingStatus.PENDING.name())){
                BookingHotelWebResponse booking = BookingHotelWebResponse.builder()
                        .bookingType(status.name())
                        .bookings(
                                bookingRepository.findByHotelAndStatus(hotel,status.name())
                                        .stream()
                                        .map(b -> {
                                            return BookingWebResponse.builder()
                                                    .bookingId(b.getId())
                                                    .customerName(b.getCustomerName())
                                                    .customerEmail(b.getCustomerEmail())
                                                    .customerPhone(b.getCustomerPhone())
                                                    .bookingDate(b.getCreatedAt())
                                                    .build();
                                        }).toList()
                        )
                        .build();
                bookingList.add(booking);
            }
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.BOOKING_DETAIL_SUCCESSFULLY)
                .data(bookingList)
                .build());
    }

    @Override
    public ResponseEntity<?> getBookingDetail(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        List<RoomBooked> roomBookedList = booking.getBookingRooms()
                .stream()
                .map(br-> {
                    System.out.println("booking id in booking detail web -> " + br.getId());
                    List<ServiceBooked> serviceBookedList = serviceRepository.findByBookingRoom(br)
                            .stream()
                            .map(serviceEntity -> {
                                BookingServiceEntity bookingService = bookingServiceEntityRepository
                                        .findByBookingAndServiceEntity(br, serviceEntity);
                                if (bookingService != null) {
                                    return ServiceBooked.builder()
                                            .serviceName(serviceEntity.getName())
                                            .serviceType(serviceEntity.getServiceType())
                                            .quantity(bookingService.getQuantity())
                                            .price(serviceEntity.getPrice().toString())
                                            .note(bookingService.getNote())
                                            .time(bookingService.getTime())
                                            .build();
                                }
                                return null;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return RoomBooked.builder()
                            .roomName(br.getRoom().getName())
                            .roomId(br.getRoom().getId())
                            .adults(br.getAdults())
                            .priceService(br.getPriceService().floatValue())
                            .priceRoom(br.getPriceRoom().floatValue())
                            .serviceSelect(serviceBookedList)
                            .build();
                }).toList();
        Set<Payment> payment = booking.getPayments();
        BookingDetailWebResponse historyDetail = BookingDetailWebResponse.builder()
                .customerName(booking.getCustomerName())
                .customerEmail(booking.getCustomerEmail())
                .customerPhone(booking.getCustomerPhone())
                .bookingStatus(booking.getStatus())
                .bookingStatusTime(booking.getUpdateAt())
                .bookingId(bookingId)
                .hotelName(booking.getHotel().getName())
                .hotelAddress(booking.getHotel().getLocation().getName())
                .totalAdults(booking.getBookingRooms().stream().mapToInt(BookingRoom::getAdults).sum())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .roomBookedList(roomBookedList)
                .totalPriceRoom(booking.getBookingRooms().stream()
                        .map(BookingRoom::getPriceRoom)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).toString())
                .priceCoupon(booking.getCouponPrice().toString())
                .totalPriceService(booking.getTotalServicePrice().toString())
                .finalPrice(booking.getTotalPrice().toString())
                .reason(
                        payment.stream()
                                .filter(
                                        pay -> pay.getPaymentType().equals(EnumPaymentType.DEPOSIT.name())
                                )
                                .findFirst()
                                .orElseThrow(()-> new AppException(ErrorCode.PAYMENT_DEPOSIT_NOT_FOUND))
                                .getMessage()

                )
                .paymentDeposit(
                        payment.stream()
                                .filter(
                                        pay -> pay.getPaymentType().equals(EnumPaymentType.DEPOSIT.name())
                                )
                                .findFirst()
                                .map(pay -> pay.getAmount().toString())
                                .orElse(null)
                )
                .paymentRemaining(
                        payment.stream()
                                .filter(
                                        pay -> pay.getPaymentType().equals(EnumPaymentType.REMAINING.name())
                                )
                                .findFirst()
                                .map(pay -> pay.getAmount().toString())
                                .orElse(null)
                )
                .build();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.HISTORY_BOOKING_DETAIL_SUCCESSFULLY)
                .data(historyDetail)
                .build());
    }

    @Override
    @Transactional
    public ResponseEntity<?> checkInStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if(LocalDateTime.now().isBefore(booking.getCheckIn())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(409)
                    .message(ErrorMessage.CHECKIN_FAILED)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        booking.setStatus(EnumBookingStatus.CHECKIN.name());
        Payment payment = booking.getPayments().stream()
                .filter(pay -> pay.getPaymentType().equals(EnumPaymentType.DEPOSIT.name()))
                .findFirst()
                .orElseThrow(()->new AppException(ErrorCode.PAYMENT_DEPOSIT_NOT_FOUND));
//        if(payment.getAmount().compareTo(booking.getTotalServicePrice()) == 0){
//            bookingRepository.save(booking);
//            return null;
//        }

//        Payment remaniningPayment = Payment.
//        BigDecimal remainingPrice = booking.getTotalPrice().subtract(payment.getAmount());
//
//        return zaloPayService.createOrder(
//                CreateOrderRequest.builder()
//                        .orderId(bookingId)
//                        .amount(remainingPrice.longValue())
//                        .paymentType(EnumPaymentType.REMAINING.name())
//                        .build()
            return null;

    }

    @Override
    public ResponseEntity<?> checkOutStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if(!booking.getStatus().equals(EnumBookingStatus.CHECKOUT.name())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .statusCode(409)
                    .message(ErrorMessage.CHECKIN_FAILED)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        return null;
    }

    @Override
    public ResponseEntity<?> cancelStatus(Long bookingId) {
        return null;
    }
}
