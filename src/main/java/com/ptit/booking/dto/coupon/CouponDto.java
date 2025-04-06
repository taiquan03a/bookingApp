package com.ptit.booking.dto.coupon;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CouponDto {
    private int id;
    private String code;
    private String description;
    private float discountValue;
    private float minBookingAmount;
    private LocalDateTime validFromDate;
    private LocalDateTime expirationDate;
}
