package com.ptit.booking.dto.coupon;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankResponse {
    private String name;
    private String description;
    private String minTotalSpent;
    private int minTotalBooking;
}
