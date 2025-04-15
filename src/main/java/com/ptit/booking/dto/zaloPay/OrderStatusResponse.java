package com.ptit.booking.dto.zaloPay;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusResponse {
    private String appTransId;
    private String status;
    private String message;
    private int returnCode;
    private String returnMessage;
    private boolean isProcessing;
    private long amount;
    private String zpTransId;
}
