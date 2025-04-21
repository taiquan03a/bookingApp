package com.ptit.booking.dto.booking;

import com.ptit.booking.model.Policy;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PaymentResponse {
    private Long bookingId;
    private List<Policy> policyList;
    private String priceDeposit;
    private String priceRemaining;
    private String totalPrice;
}
