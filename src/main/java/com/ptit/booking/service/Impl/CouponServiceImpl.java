package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.coupon.CouponDto;
import com.ptit.booking.dto.coupon.RankResponse;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.mapping.CouponMapper;
import com.ptit.booking.model.Coupon;
import com.ptit.booking.model.Rank;
import com.ptit.booking.model.User;
import com.ptit.booking.repository.CouponRepository;
import com.ptit.booking.repository.RankRepository;
import com.ptit.booking.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;
    private final RankRepository rankRepository;

    @Override
    public ResponseEntity<?> getCouponByUser(Principal principal, String couponCode, float totalPrice) {
        User user = (principal != null) ? (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal() : null;
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        //List<Coupon> couponListByUser = couponRepository.findByUser(user);
        List<CouponDto> couponDtoListUse = couponMapper.toDtoList(
                couponRepository.findBestCouponByUser(couponCode,user, BigDecimal.valueOf(totalPrice))
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

    @Override
    public ResponseEntity<?> getRankingInfo() {
        List<Rank> rankList = rankRepository.findAll();
        List<RankResponse> rankResponseList = rankList.stream()
                .sorted(Comparator.comparing(Rank::getRankLevel))
                .map(rank -> {
                    return RankResponse.builder()
                            .name(rank.getName())
                            .description(rank.getDescription())
                            .minTotalSpent(rank.getMinTotalSpent().toString())
                            .minTotalBooking(rank.getMinTotalBooking())
                            .build();
                }).toList();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.LIST_RANKING_SUCCESSFULLY)
                .data(rankResponseList)
                .build());
    }

    @Override
    public ResponseEntity<?> currentRanking(Principal principal) {
        return null;
    }

    @Override
    public ResponseEntity<?> myCoupon(Principal principal) {
        return null;
    }

    @Override
    public ResponseEntity<?> saveCoupon(Principal principal, Long voucherId) {
        return null;
    }
}
