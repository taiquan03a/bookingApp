package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.coupon.CouponDto;
import com.ptit.booking.mapping.CouponMapper;
import com.ptit.booking.model.Coupon;
import com.ptit.booking.model.User;
import com.ptit.booking.repository.CouponRepository;
import com.ptit.booking.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Override
    public ResponseEntity<?> getCouponByUser(Principal principal,String couponCode) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        List<Coupon> couponListByUser = couponRepository.findByUser(user);
        List<CouponDto> couponDtoListUse = couponMapper.toDtoList(
                couponListByUser
                        .stream()
                        .filter(coupon -> coupon.getExpiryDate().isBefore(LocalDateTime.now()))
                        .filter(coupon -> coupon.getValidFromDate().isAfter(LocalDateTime.now()))
                        .filter(coupon -> coupon.getMaxUsage() >= coupon.getCurrentUsage())
                        .filter(Coupon::getStatus)
                        .toList()
        );
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.LIST_COUPON_CAN_USE)
                .data(couponDtoListUse)
                .build());
    }


    @Override
    public ResponseEntity<?> getCouponForBooking(Principal principal, Long bookingId) {
        return null;
    }
}
