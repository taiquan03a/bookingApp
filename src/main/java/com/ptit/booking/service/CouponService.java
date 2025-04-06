package com.ptit.booking.service;

import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface CouponService {
    ResponseEntity<?> getCouponByUser(Principal principal);
    ResponseEntity<?> getCouponForBooking(Principal principal,Long bookingId);
}
