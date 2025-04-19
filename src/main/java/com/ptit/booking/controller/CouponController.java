package com.ptit.booking.controller;

import com.ptit.booking.service.CouponService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/coupon")
public class CouponController {
    private final CouponService couponService;

    @GetMapping("get_by_user")
    public ResponseEntity<?> addCoupon(@RequestParam(required = false,defaultValue = "") String code,
                                       @RequestParam float totalPrice,
                                       Principal principal) {
        return couponService.getCouponByUser(principal,code,totalPrice);
    }

    @GetMapping("rank")
    public ResponseEntity<?> getRanking(){
        return  couponService.getRankingInfo();
    }

    @GetMapping("member")
    public ResponseEntity<?> getMember(Principal principal){
        return couponService.currentRanking(principal);
    }

    @GetMapping("user_save/{couponId}")
    public ResponseEntity<?> saveUser(Principal principal, @PathVariable long couponId){
        return couponService.saveCoupon(principal,couponId);
    }
    @GetMapping("my_coupon")
    public ResponseEntity<?> getMyCoupon(Principal principal){
        return couponService.myCoupon(principal);
    }
}
