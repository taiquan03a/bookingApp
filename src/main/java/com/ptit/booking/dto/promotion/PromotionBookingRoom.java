package com.ptit.booking.dto.promotion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromotionBookingRoom {
    private Long id;
    private String name;
    private String discountValue;
}
