package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.ErrorMessage;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.coupon.CouponDto;
import com.ptit.booking.dto.coupon.OverViewRanking;
import com.ptit.booking.dto.coupon.RankResponse;
import com.ptit.booking.enums.EnumBookingStatus;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.exception.ErrorResponse;
import com.ptit.booking.mapping.CouponMapper;
import com.ptit.booking.model.*;
import com.ptit.booking.repository.CouponRepository;
import com.ptit.booking.repository.RankRepository;
import com.ptit.booking.repository.UserCouponRepository;
import com.ptit.booking.repository.UserRepository;
import com.ptit.booking.service.CouponService;
import jakarta.transaction.Transactional;
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

import static com.ptit.booking.constants.ErrorMessage.INCORRECT_PASSWORD;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;
    private final RankRepository rankRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;

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
        var user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        List<Booking> bookingUser = userRepository.findBookingByUser(user);
        int totalBooking =  bookingUser.size();

        // chi lay cac don da duoc check out
        BigDecimal totalSpent = bookingUser
                .stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Rank> ranks = rankRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Rank::getRankLevel))
                .toList();

        Rank currentRank = ranks.stream()
                .filter(rank ->
                        totalSpent.compareTo(rank.getMinTotalSpent()) >= 0 &&
                                totalBooking >= rank.getMinTotalBooking()
                )
                .max(Comparator.comparing(Rank::getMinTotalSpent)) // lấy rank cao nhất mà user đạt
                .orElse(null);

        if(currentRank == null) {
            return null;
        }
        Rank nextRank = rankRepository.findByRankLevel(currentRank.getRankLevel() + 1);
        user.setRank(currentRank);
        userRepository.save(user);
        OverViewRanking overViewRanking = OverViewRanking.builder()
                .ranking(currentRank.getName())
                .currentTotalSpent(totalSpent.toString())
                .currentTotalBooking(totalBooking)
                .minTotalSpent(nextRank.getMinTotalSpent().toString())
                .minTotalBooking(nextRank.getMinTotalBooking())
                .couponList(
                        couponMapper.toDtoList(currentRank.getCoupons())
                )
                .build();
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.LIST_RANKING_SUCCESSFULLY)
                .data(overViewRanking)
                .build());
    }

    @Override
    public ResponseEntity<?> myCoupon(Principal principal) {
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<?> saveCoupon(Principal principal, Long voucherId) {
        var user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message(ErrorMessage.PLEASE_LOGIN)
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build());
        }
        Coupon coupon = couponRepository.findById(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        if(user.getRank().getId() != coupon.getRank().getId()) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .statusCode(450)
                            .message(ErrorMessage.COUPON_NOT_IN_RANK)
                            .timestamp(new Date(System.currentTimeMillis()))
                            .build()
            );
        }
        userCouponRepository.save(
                UserCoupon.builder()
                        .user(user)
                        .coupon(coupon)
                        .use(false)
                        .assignedDate(LocalDateTime.now())
                        .build()
        );
        return ResponseEntity.ok(ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message(SuccessMessage.SAVE_COUPON_SUCCESSFULLY)
                .data(coupon)
                .build());
    }
}
