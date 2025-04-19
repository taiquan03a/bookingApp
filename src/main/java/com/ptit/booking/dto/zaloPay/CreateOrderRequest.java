package com.ptit.booking.dto.zaloPay;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderRequest {
    private Long orderId;
    private long amount;
    private String paymentType;
}