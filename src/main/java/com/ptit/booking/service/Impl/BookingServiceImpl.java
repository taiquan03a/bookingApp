package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.booking.BookingDetail;
import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.dto.booking.CancelBookingRequest;
import com.ptit.booking.dto.booking.HistoryBooking;
import com.ptit.booking.dto.hotel.HotelHistoryResponse;
import com.ptit.booking.dto.room.RoomBooked;
import com.ptit.booking.dto.room.RoomRequest;
import com.ptit.booking.dto.serviceRoom.ServiceBooked;
import com.ptit.booking.dto.serviceRoom.ServiceRoomDto;
import com.ptit.booking.dto.zaloPay.RefundOrderRequest;
import com.ptit.booking.dto.zaloPay.RefundResponse;
import com.ptit.booking.enums.EnumBookingStatus;
import com.ptit.booking.enums.EnumPaymentType;
import com.ptit.booking.enums.EnumPolicyOperator;
import com.ptit.booking.enums.EnumPolicyType;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.*;
import com.ptit.booking.service.BookingService;
import com.ptit.booking.service.ZaloPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.ptit.booking.constants.ErrorMessage.EMAIL_IN_USE;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final RoomRepository roomRepository;
    private final CouponRepository couponRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ZaloPayService zaloPayService;
    private final PaymentRepository paymentRepository;

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
                ServiceEntity serviceEntity = serviceRepository.findById(service.getId())
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

    @Override
    public ResponseEntity<?> historyBooking(Principal principal) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        List<HistoryBooking> historyBookingList = new ArrayList<>();
        for (EnumBookingStatus status : EnumBookingStatus.values()) {
            if(!status.equals(EnumBookingStatus.PENDING)){
                List<Booking> bookedList = bookingRepository.findAllByUserAndStatus(user,status.name());
                List<HotelHistoryResponse> hotelBookedList = bookedList.stream().map(booking -> {
                    Hotel hotel = booking.getHotel();
                    return HotelHistoryResponse.builder()
                            .bookingId(booking.getId())
                            .hotelName(hotel.getName())
                            .rating(hotel.getRating())
                            .feedbackSum(hotel.getFeedbackSum())
                            .bookingDate(booking.getCreatedAt())
                            .bookingPrice(booking.getTotalPrice().toString())
                            .image(hotel.getImages()
                                    .stream()
                                    .findFirst()
                                    .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND))
                                    .getUrl())
                            .build();
                }).toList();

                HistoryBooking historyBooking = HistoryBooking.builder()
                        .bookingStatus(status.name())
                        .hotelBookingList(hotelBookedList)
                        .build();
                historyBookingList.add(historyBooking);
            }
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.HISTORY_BOOKING_SUCCESSFULLY)
                .data(historyBookingList)
                .build());
    }

    @Override
    public ResponseEntity<?> cancelBooking(CancelBookingRequest cancelBookingRequest, Principal principal) throws Exception {
        Booking booking = bookingRepository.findById(cancelBookingRequest.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if(!booking.getStatus().equals(EnumBookingStatus.BOOKED.name())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ErrorResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(ErrorMessage.HOTEL_NOT_BOOKED)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        Hotel hotel = booking.getHotel();
        Policy policyCancel = hotelRepository.findPoliciesByHotelAndType(hotel, EnumPolicyType.CANCEL.name());

        LocalDateTime checkInTime = booking.getCheckIn();
        LocalDateTime currentTime = LocalDateTime.now();
        long hoursBetween = Math.abs(Duration.between(currentTime, checkInTime).toHours());

        BigDecimal refundPrice = BigDecimal.ZERO;

        //hoursBetween sau 12h thi theo value , truoc 12h thi 100%
        if(policyCancel.getOperator().equals(EnumPolicyOperator.after.name())){
            // sau 12h
            if(hoursBetween < Long.parseLong(policyCancel.getCondition())){
                String numericPart = policyCancel.getValue().replace("%", "").trim();
                BigDecimal refundPercent = new BigDecimal(numericPart).divide(BigDecimal.valueOf(100));
                refundPrice = booking.getTotalPrice().multiply(refundPercent);
            }else{
                // >= 12h
                refundPrice = booking.getTotalPrice();
            }
        }
        //hoursBetween truoc 12h thi theo value , sau 12h thi 0%
        else if(policyCancel.getOperator().equals(EnumPolicyOperator.before.name())){
                // >12h
                if(hoursBetween > Long.parseLong(policyCancel.getCondition())){
                    String numericPart = policyCancel.getValue().replace("%", "").trim();
                    BigDecimal refundPercent = new BigDecimal(numericPart).divide(BigDecimal.valueOf(100));
                    refundPrice = booking.getTotalPrice().multiply(refundPercent);
                }
        }
        Payment paymentDeposit = paymentRepository.findByBooking(booking)
                .stream()
                .filter(payment -> payment.getPaymentType().equals(EnumPaymentType.DEPOSIT.name()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_DEPOSIT_NOT_FOUND));

        RefundOrderRequest refundOrderRequest = new RefundOrderRequest();
        refundOrderRequest.setZpTransId(paymentDeposit.getZpTransId());
        refundOrderRequest.setAmount(paymentDeposit.getAmount().longValue());
        refundOrderRequest.setDescription(cancelBookingRequest.getReason());
        RefundResponse refundResponse = zaloPayService.refundOrder(refundOrderRequest);
        if(refundResponse.getReturnCode() == 1){
            return ResponseEntity.ok(ApiResponse.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message(SuccessMessage.REFUND_BOOKING_SUCCESSFULLY)
                    .data(refundResponse)
                    .build());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .statusCode(2010)
                .message(ErrorMessage.REFUND_BOOKING_FAIL)
                .description(EMAIL_IN_USE)
                .timestamp(new Date(System.currentTimeMillis()))
                .build());
    }
}
