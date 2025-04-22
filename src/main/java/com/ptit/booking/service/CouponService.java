package com.ptit.booking.service;

import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface CouponService {
    ResponseEntity<?> getCouponByUser(Principal principal, String couponCode, float totalPrice,Long couponCurrentId);
    ResponseEntity<?> getCouponForBooking(Principal principal,Long bookingId);
    ResponseEntity<?> getRankingInfo();
    ResponseEntity<?> currentRanking(Principal principal);
    ResponseEntity<?> myCoupon(Principal principal);
    ResponseEntity<?> saveCoupon(Principal principal, Long voucherId);
    ResponseEntity<?> couponEvent();
    //ResponseEntity<?> filterByCode(String code);
}
