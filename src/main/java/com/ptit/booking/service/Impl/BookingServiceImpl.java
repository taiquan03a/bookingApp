package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.NotificationConstants;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.booking.*;
import com.ptit.booking.dto.hotel.HotelHistoryResponse;
import com.ptit.booking.dto.room.RoomBooked;
import com.ptit.booking.dto.room.RoomRequest;
import com.ptit.booking.dto.serviceRoom.ServiceBooked;
import com.ptit.booking.dto.serviceRoom.ServiceRoomDto;
import com.ptit.booking.dto.zaloPay.RefundOrderRequest;
import com.ptit.booking.dto.zaloPay.RefundResponse;
import com.ptit.booking.enums.*;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.*;
import com.ptit.booking.service.BookingService;
import com.ptit.booking.service.NotificationService;
import com.ptit.booking.service.ZaloPayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.ptit.booking.constants.ErrorMessage.EMAIL_IN_USE;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private final RoomRepository roomRepository;
    private final CouponRepository couponRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ZaloPayService zaloPayService;
    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final NotificationService notificationService;

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
        BigDecimal totalPriceRoom = BigDecimal.ZERO;
        BigDecimal totalPriceService = BigDecimal.ZERO;
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
            BigDecimal priceService = BigDecimal.ZERO;
            for(ServiceRoomDto service: roomRequest.getServiceList()){
                ServiceEntity serviceEntity = serviceRepository.findById(service.getId())
                        .orElseThrow(()->new AppException(ErrorCode.SERVICE_NOT_FOUND));
                BigDecimal priceServiceCurrentRoom = serviceEntity.getPrice().multiply(BigDecimal.valueOf(service.getQuantity()));
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
                priceService = priceService.add(priceServiceCurrentRoom);
            }

            RoomBooked roomBooked = RoomBooked.builder()
                    .roomId(roomSelect.getId())
                    .roomName(roomSelect.getName())
                    .adults(roomRequest.getAdults())
                    .serviceSelect(serviceBookedList)
                    .policyBooked(policyByHotelList)
                    .priceRoom(BigDecimal.valueOf(roomRequest.getPrice()))
                    .priceService(priceService)
                    .build();
            totalPriceRoom = totalPriceRoom.add(roomBooked.getPriceRoom());
            totalPriceService = totalPriceService.add(priceService);
            totalAdults += roomBooked.getAdults();
            roomBookedList.add(roomBooked);
        }
        //long selectDay = ChronoUnit.DAYS.between(bookingRoomRequest.getCheckInDate(), bookingRoomRequest.getCheckOutDate());
        BigDecimal totalPrice = totalPriceRoom.add(totalPriceService);
        BigDecimal priceCoupon = BigDecimal.ZERO;
        Coupon coupon = new Coupon();
        System.out.println("couponId: " + bookingRoomRequest.getCouponId());
        if(bookingRoomRequest.getCouponId() == 0){
            List<Coupon> couponList = couponRepository
                    .findBestCouponByUser(
                            "",
                            user,
                            totalPrice
                    );
            if(!couponList.isEmpty()){
                coupon = couponList.get(0);
                System.out.println("coupon: " + coupon.getCode());
                priceCoupon = couponRepository.calculateDiscountAmount(
                        coupon,totalPrice);
            }
        }else{
            coupon = couponRepository.findById(bookingRoomRequest.getCouponId())
                    .orElseThrow(()-> new AppException(ErrorCode.COUPON_NOT_FOUND));
            priceCoupon = couponRepository.calculateDiscountAmount(
                    coupon,totalPrice);
        }
        Policy policyPayment = hotelRepository.findPoliciesByHotelAndType(hotel, EnumPolicyType.PAYMENT.name());

        BigDecimal finalPrice = totalPrice.subtract(priceCoupon);
        BigDecimal paymentPrice = finalPrice;
        System.out.println("finalPrice: " + finalPrice);
        if(policyPayment.getOperator().equals(EnumPolicyOperator.equals.name())){
            String numericPart = policyPayment.getValue().replace("%", "").trim();
            BigDecimal paymentPercent = new BigDecimal(numericPart).divide(BigDecimal.valueOf(100));
            System.out.println("payment percent: " + paymentPercent);
            paymentPrice = finalPrice.multiply(paymentPercent);
            System.out.println("payment price: " + paymentPrice);
        }

        BookingDetail bookingDetail = BookingDetail.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelAddress(hotel.getLocation().getName())
                .totalAdults(totalAdults)
                .policyPayment(policyPayment.getDescription())
                .checkIn(bookingRoomRequest.getCheckInDate().atTime(hotel.getCheckInTime()))
                .checkOut(bookingRoomRequest.getCheckOutDate().atTime(hotel.getCheckOutTime()))
                .roomBookedList(roomBookedList)
                .couponId(coupon.getId())
                .couponCode(coupon.getCode())
                .priceDeposit(paymentPrice.setScale(2, RoundingMode.HALF_UP).toString())
                .priceCoupon(priceCoupon.setScale(2, RoundingMode.HALF_UP).toString())
                .totalPriceRoom(totalPriceRoom.setScale(2, RoundingMode.HALF_UP).toString())
                .totalPriceService(totalPriceService.setScale(2, RoundingMode.HALF_UP).toString())
                .finalPrice(finalPrice.setScale(2, RoundingMode.HALF_UP).toString())
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
            if(!status.equals(EnumBookingStatus.PENDING) && !status.equals(EnumBookingStatus.FAILED)){
                List<Booking> bookedList = bookingRepository.findAllByUserAndStatus(user,status.name());
                List<HotelHistoryResponse> hotelBookedList = bookedList.stream().map(booking -> {
                    Hotel hotel = booking.getHotel();
                    return HotelHistoryResponse.builder()
                            .hotelId(hotel.getId())
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
    @Transactional
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


        LocalDateTime checkInTime = booking.getCheckIn();
        LocalDateTime currentTime = LocalDateTime.now();
        long hoursBetween = Duration.between(currentTime, checkInTime).toHours();
        if(hoursBetween <= 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ErrorResponse.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(ErrorMessage.CURRENT_TIME_MORE_THAN_CHECKIN)
                            .timestamp(new Date(System.currentTimeMillis()))
                            .build()
            );
        }


        Payment paymentDeposit = paymentRepository.findByBooking(booking)
                .stream()
                .filter(payment -> payment.getPaymentType().equals(EnumPaymentType.DEPOSIT.name()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_DEPOSIT_NOT_FOUND));


        BigDecimal refundPrice = calculateRefundPrice(booking,paymentDeposit,hoursBetween);

        Payment paymentCancel = Payment.builder()
                .amount(refundPrice)
                .paymentMethod("ZALOPAY")
                .paymentType(EnumPaymentType.REFUND.name())
                .booking(booking)
                .createdAt(LocalDateTime.now())
                .message(cancelBookingRequest.getReason())
                .paymentStatus(EnumBookingStatus.PENDING.name())
                .zpTransId(paymentDeposit.getZpTransId())
                .build();
        if(refundPrice.compareTo(BigDecimal.ZERO) == 0){
            //paymentDeposit.setPaymentStatus(EnumBookingStatus.CANCELED.name());
            //paymentDeposit.setMessage(cancelBookingRequest.getReason());
            booking.setStatus(EnumBookingStatus.CANCELED.name());
            paymentCancel.setPaymentStatus(EnumPaymentStatus.SUCCESS.name());
            paymentRepository.save(paymentCancel);
            bookingRepository.save(booking);
            String title = NotificationConstants.Template.Cancel.TITLE_SUCCESS;
            String message = String.format(
                    NotificationConstants.Template.Cancel.MESSAGE_SUCCESS,
                    booking.getHotel().getName(), booking.getCheckIn(), booking.getCheckOut()
            );
            notificationService.sendNotification(
                    booking.getUser().getId(),
                    title,
                    message,
                    EnumNotificationType.BOOKING
            );
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.builder()
                            .message(SuccessMessage.REFUND_BOOKING_SUCCESSFULLY)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            );
        }

        RefundOrderRequest refundOrderRequest = new RefundOrderRequest();
        refundOrderRequest.setZpTransId(paymentDeposit.getZpTransId());
        refundOrderRequest.setAmount(refundPrice.longValue());
        refundOrderRequest.setDescription(cancelBookingRequest.getReason());
        RefundResponse refundResponse = zaloPayService.refundOrder(refundOrderRequest);
        if(refundResponse.getReturnCode() == 1){
//            paymentDeposit.setPaymentStatus(EnumBookingStatus.CANCELED.name());
//            paymentDeposit.setMessage(cancelBookingRequest.getReason());
            paymentCancel.setPaymentStatus(EnumPaymentStatus.SUCCESS.name());
            booking.setStatus(EnumBookingStatus.CANCELED.name());
            paymentRepository.save(paymentCancel);
            bookingRepository.save(booking);
            String title = NotificationConstants.Template.Cancel.TITLE_SUCCESS;
            String message = String.format(
                    NotificationConstants.Template.Cancel.MESSAGE_SUCCESS,
                    booking.getHotel().getName(), booking.getCheckIn(), booking.getCheckOut()
            );
            notificationService.sendNotification(
                    booking.getUser().getId(),
                    title,
                    message,
                    EnumNotificationType.BOOKING
            );
            return ResponseEntity.ok(ApiResponse.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message(SuccessMessage.REFUND_BOOKING_SUCCESSFULLY)
                    .data(refundResponse)
                    .build());
        }
        paymentCancel.setPaymentStatus(EnumPaymentStatus.FAILED.name());
        paymentRepository.save(paymentCancel);
        String title = NotificationConstants.Template.Cancel.TITLE_FAIL;
        String message = String.format(
                NotificationConstants.Template.Cancel.MESSAGE_FAIL,
                booking.getHotel().getName()
        );
        notificationService.sendNotification(
                booking.getUser().getId(),
                title,
                message,
                EnumNotificationType.BOOKING
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .statusCode(2010)
                .message(ErrorMessage.REFUND_BOOKING_FAIL)
                .timestamp(new Date(System.currentTimeMillis()))
                .build());
    }

    @Override
    public ResponseEntity<?> historyBookingDetail(Long bookingId, Principal principal) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        List<RoomBooked> roomBookedList = booking.getBookingRooms()
                .stream()
                .map(br-> {
                    List<ServiceBooked> serviceBookedList = serviceRepository.findByBookingRoom(br)
                            .stream()
                            .map(serviceEntity -> {
                                return ServiceBooked.builder()
                                        .serviceName(serviceEntity.getName())
                                        .serviceType(serviceEntity.getServiceType())
                                        .build();
                            }).toList();
                    return RoomBooked.builder()
                            .roomName(br.getRoom().getName())
                            .roomId(br.getRoom().getId())
                            .adults(br.getAdults())
                            .priceService(br.getPriceService())
                            .priceRoom(br.getPriceRoom())
                            .serviceSelect(serviceBookedList)
                            .build();
                }).toList();
        Set<Payment> payment = booking.getPayments();

        /*
            Note: Tinh tien hoan neu nguoi dung co nhu cau huy
         */
        LocalDateTime checkInTime = booking.getCheckIn();
        LocalDateTime currentTime = LocalDateTime.now();
        long hoursBetween = Duration.between(currentTime, checkInTime).toHours();
        Payment paymentDeposit = paymentRepository.findByBooking(booking)
                .stream()
                .filter(pay -> pay.getPaymentType().equals(EnumPaymentType.DEPOSIT.name()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_DEPOSIT_NOT_FOUND));


        BigDecimal refundPrice = calculateRefundPrice(booking,paymentDeposit,hoursBetween);

        HistoryBookingDetailResponse historyDetail = HistoryBookingDetailResponse.builder()
                .hotelId(booking.getHotel().getId())
                .bookingStatus(booking.getStatus())
                .bookingStatusTime(booking.getUpdateAt())
                .cancelTime(
                        payment.stream()
                                .filter(
                                        pay -> pay.getPaymentType().equals(EnumPaymentType.REFUND.name())
                                )
                                .findFirst()
                                .map(Payment::getCreatedAt)
                                .orElse(null)
                )
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
                                        pay -> pay.getPaymentType().equals(EnumPaymentType.REFUND.name())
                                )
                                .findFirst()
                                .map(Payment::getMessage)
                                .orElse(null)

                )
                .priceIfRefund(refundPrice.setScale(2, RoundingMode.HALF_UP).toString())
                .paymentRefund(
                        payment.stream()
                                .filter(
                                        pay -> pay.getPaymentType().equals(EnumPaymentType.REFUND.name())
                                )
                                .findFirst()
                                .map(pay -> pay.getAmount().toString())
                                .orElse(null)
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
                .policyList(
                        policyRepository.findCancelByHotel(booking.getHotel())
                )
                .build();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.HISTORY_BOOKING_DETAIL_SUCCESSFULLY)
                .data(historyDetail)
                .build());
    }

    private BigDecimal calculateRefundPrice(Booking booking,Payment paymentDeposit,long hoursBetween){
        Policy policyCancel = hotelRepository.findPoliciesByHotelAndType(booking.getHotel(), EnumPolicyType.CANCEL.name());

        BigDecimal refundPrice = BigDecimal.ZERO;

        //hoursBetween sau 12h thi theo value , truoc 12h thi 100%
        if(policyCancel.getOperator().equals(EnumPolicyOperator.after.name())){
            // sau 12h
            if(hoursBetween < Long.parseLong(policyCancel.getCondition())){
                String numericPart = policyCancel.getValue().replace("%", "").trim();
                BigDecimal refundPercent = new BigDecimal(numericPart).divide(BigDecimal.valueOf(100));
                refundPrice = paymentDeposit.getAmount().multiply(refundPercent);
            }else{
                // >= 12h
                refundPrice = paymentDeposit.getAmount();
            }
        }
        //hoursBetween truoc 12h thi theo value , sau 12h thi 0%
        else if(policyCancel.getOperator().equals(EnumPolicyOperator.before.name())){
            // >12h
            if(hoursBetween > Long.parseLong(policyCancel.getCondition())){
                String numericPart = policyCancel.getValue().replace("%", "").trim();
                BigDecimal refundPercent = new BigDecimal(numericPart).divide(BigDecimal.valueOf(100));
                refundPrice = paymentDeposit.getAmount().multiply(refundPercent);
            }
        }

        log.info("refund price: " + refundPrice);
        return refundPrice;
    }
}
