package com.ptit.booking.dto.zaloPay;

import lombok.Data;

@Data
public class RefundOrderRequest {
    private String zpTransId;
    private long amount;
    private String description;
}
