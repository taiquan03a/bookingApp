package com.ptit.booking.controller;

import com.ptit.booking.service.CouponService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/coupon")
public class CouponController {
    private final CouponService couponService;

    @GetMapping("get_by_user")
    public ResponseEntity<?> addCoupon(@RequestParam String code, Principal principal) {
        return couponService.getCouponByUser(principal,code);
    }

}
