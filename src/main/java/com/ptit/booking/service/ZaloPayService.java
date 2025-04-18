package com.ptit.booking.service;

import com.ptit.booking.dto.zaloPay.CreateOrderRequest;
import com.ptit.booking.dto.zaloPay.RefundOrderRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ZaloPayService {
    ResponseEntity<?> createOrder(CreateOrderRequest request);
    Map<String, Object> handleCallback(String jsonStr);
    ResponseEntity<?> getOrderStatus(String appTransId);
    ResponseEntity<?> refundOrder(RefundOrderRequest request) throws Exception;
    ResponseEntity<?> getRefundStatus(String appTransId) throws Exception;
}
