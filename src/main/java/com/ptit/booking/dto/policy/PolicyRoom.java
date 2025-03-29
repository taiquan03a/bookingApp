package com.ptit.booking.dto.policy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PolicyRoom {
    private Long policyId;
    private String policyName;
    private String policyDescription;
}

