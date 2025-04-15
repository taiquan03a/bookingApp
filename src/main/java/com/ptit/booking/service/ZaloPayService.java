package com.ptit.booking.service;

import com.ptit.booking.dto.zaloPay.CreateOrderRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ZaloPayService {
    public ResponseEntity<?> createOrder(CreateOrderRequest request);
    public Map<String, Object> handleCallback(String jsonStr);
    public ResponseEntity<?> getOrderStatus(String appTransId);
}
