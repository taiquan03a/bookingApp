package com.ptit.booking.dto.zaloPay;

import lombok.Data;

@Data
public class RefundStatusResponse {
    private int returnCode;
    private String returnMessage;
    private int subReturnCode;
    private String subReturnMessage;
}
