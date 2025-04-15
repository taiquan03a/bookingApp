package com.ptit.booking.dto.zaloPay;


import lombok.Data;

@Data
public class CreateOrderRequest {
    private String orderId;
    private long amount;
}