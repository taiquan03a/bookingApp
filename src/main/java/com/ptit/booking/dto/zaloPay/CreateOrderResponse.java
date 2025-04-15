package com.ptit.booking.dto.zaloPay;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderResponse {
    private String zpTransToken;
    private String appTransId;
    private int returnCode;
    private String orderUrl;
    private String returnMessage;
}
