package com.ptit.booking.dto.coupon;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyCouponResponse {
    private String couponStatus;
    List<CouponDto> couponList;
}
