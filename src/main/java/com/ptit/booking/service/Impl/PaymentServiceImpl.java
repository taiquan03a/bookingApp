package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.PaymentBookingRequest;
import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.dto.booking.PaymentResponse;
import com.ptit.booking.dto.room.RoomRequest;
import com.ptit.booking.dto.serviceRoom.ServiceRoomDto;
import com.ptit.booking.dto.zaloPay.CreateOrderRequest;
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
import com.ptit.booking.service.PaymentService;
import com.ptit.booking.service.ZaloPayService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final ZaloPayService zaloPayService;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final ServiceRepository serviceRepository;
    private final CouponRepository couponRepository;

    @Override
    @Transactional()
    public ResponseEntity<?> checkout(PaymentBookingRequest bookingRoomRequest, Principal principal) {
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
                .checkIn(bookingRoomRequest.getCheckInDate().atTime(hotel.getCheckInTime()))
                .checkOut(bookingRoomRequest.getCheckOutDate().atTime(hotel.getCheckOutTime()))
                .status(EnumBookingStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .roomCount(bookingRoomRequest.getRoomRequestList().size())
                .totalPrice(BigDecimal.ZERO)
                .promotionPrice(BigDecimal.ZERO)
                .couponPrice(BigDecimal.ZERO)
                .totalServicePrice(BigDecimal.ZERO)
                .customerEmail(bookingRoomRequest.getCustomerEmail())
                .customerName(bookingRoomRequest.getCustomerName())
                .customerPhone(bookingRoomRequest.getCustomerPhone())
                .build();
        bookingRepository.save(booking);

        List<BookingRoom> bookingRoomList = new ArrayList<>();

        BigDecimal totalPriceRoom = BigDecimal.ZERO;
        BigDecimal totalRoomNotPromotion = BigDecimal.ZERO;
        BigDecimal totalServicePrice = BigDecimal.ZERO;

        for(RoomRequest roomRequest : bookingRoomRequest.getRoomRequestList()){
            Room room = roomRepository.findById(roomRequest.getRoomId())
                    .orElseThrow(()->new AppException(ErrorCode.ROOM_NOT_FOUND));
            totalRoomNotPromotion = totalRoomNotPromotion.add(room.getPrice());
            BigDecimal priceServiceRoom = BigDecimal.ZERO;
            BigDecimal priceRoom = BigDecimal.valueOf(roomRequest.getPrice());
            List<BookingServiceEntity> bookingServiceList = new ArrayList<>();

            // Lấy danh sách ID dịch vụ
            Set<Long> serviceIds = roomRequest.getServiceList().stream()
                    .map(ServiceRoomDto::getId)
                    .collect(Collectors.toSet());

            // Lấy tất cả ServiceEntity trong một truy vấn duy nhất
            List<ServiceEntity> serviceEntities = serviceRepository.findAllById(serviceIds);

            Map<Long, ServiceEntity> serviceEntityMap = serviceEntities.stream()
                    .collect(Collectors.toMap(ServiceEntity::getId, Function.identity()));
            for(ServiceRoomDto serviceRoomDto : roomRequest.getServiceList()){

                ServiceEntity serviceEntity  = serviceEntityMap.get(serviceRoomDto.getId());

                BigDecimal priceService = serviceEntity.getPrice()
                        .multiply(BigDecimal.valueOf(serviceRoomDto.getQuantity()));

                priceServiceRoom = priceServiceRoom.add(priceService);

                BookingServiceEntity bookingService = BookingServiceEntity.builder()
                        .service(serviceEntity)
                        .time(serviceRoomDto.getTime())
                        .note(serviceRoomDto.getNote())
                        .quantity(serviceRoomDto.getQuantity())
                        .totalPrice(priceService)
                        .creatAt(LocalDateTime.now())
                        .build();
                bookingServiceList.add(bookingService);
            }
            BookingRoom bookingRoom = BookingRoom.builder()
                    .room(room)
                    .booking(booking)
                    .adults(roomRequest.getAdults())
                    .children(roomRequest.getChildren())
                    .priceRoom(BigDecimal.valueOf(roomRequest.getPrice()))
                    .priceService(priceServiceRoom)
                    .booingServices(new ArrayList<>())
                    .build();
            for(BookingServiceEntity bookingServiceEntity : bookingServiceList){
                bookingServiceEntity.setBooking(bookingRoom);
            }
            bookingRoom.setBooingServices(bookingServiceList);
            log.info("Number of services for bookingRoom: {}", bookingRoom.getBooingServices().size());
            bookingRoomList.add(bookingRoom);
            totalPriceRoom = totalPriceRoom.add(priceRoom);
            totalServicePrice = totalServicePrice.add(priceServiceRoom);
        }

        long selectDay = ChronoUnit.DAYS.between(bookingRoomRequest.getCheckInDate(), bookingRoomRequest.getCheckOutDate());
        //totalPriceRoom = totalPriceRoom.multiply(BigDecimal.valueOf(selectDay));
        BigDecimal couponPrice = BigDecimal.ZERO;
        if(bookingRoomRequest.getCouponId() != 0){
             couponPrice = couponRepository.calculateDiscountAmount(
                    couponRepository.findById(bookingRoomRequest.getCouponId())
                            .orElseThrow(()-> new AppException(ErrorCode.COUPON_NOT_FOUND)),
                     totalPriceRoom.add(totalServicePrice)
            );
        }

        booking.setTotalServicePrice(totalServicePrice);
        booking.setPromotionPrice(totalRoomNotPromotion.subtract(totalPriceRoom));
        booking.setCouponPrice(couponPrice);
        booking.setTotalPrice(totalPriceRoom.subtract(couponPrice));
        bookingRepository.save(booking);
        for (BookingRoom room : bookingRoomList) {
            room.setBooking(booking);
        }
        bookingRoomRepository.saveAll(bookingRoomList);

        BigDecimal paymentPrice = booking.getTotalPrice();
        Policy policyPayment = hotelRepository.findPoliciesByHotelAndType(hotel, EnumPolicyType.PAYMENT.name());
        if(policyPayment.getOperator().equals(EnumPolicyOperator.equals.name())){
            String numericPart = policyPayment.getValue().replace("%", "").trim();
            BigDecimal paymentPercent = new BigDecimal(numericPart).divide(BigDecimal.valueOf(100));
            System.out.println("payment percent: " + paymentPercent);
            paymentPrice = booking.getTotalPrice().multiply(paymentPercent);
            System.out.println("payment price: " + paymentPrice);
        }
        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .orderId(booking.getId())
                .amount(paymentPrice.longValue())
                .paymentType(EnumPaymentType.DEPOSIT.name())
                .build();
        Coupon coupon = couponRepository.findById(bookingRoomRequest.getCouponId())
                .orElseThrow(()-> new AppException(ErrorCode.COUPON_NOT_FOUND));
        //UserCoupon userCoupon = coupon.getUserCoupons();

        return zaloPayService.createOrder(createOrderRequest);
    }
}
