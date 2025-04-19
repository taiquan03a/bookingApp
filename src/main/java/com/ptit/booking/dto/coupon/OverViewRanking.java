package com.ptit.booking.dto.coupon;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OverViewRanking {
    private String ranking;
    private int currentTotalBooking;
    private String currentTotalSpent;
    private int minTotalBooking;
    private String minTotalSpent;
    List<CouponDto> couponList;
}
