package com.ptit.booking.dto.zaloPay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundResponse {
    private int returnCode;
    private String returnMessage;
    private int subReturnCode;
    private String subReturnMessage;
    private String refundId;
    private String zpTransId;
    private String mRefundId;
    private long refundAmount;
}

