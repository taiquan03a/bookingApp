package com.ptit.booking.dto.serviceRoom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceService {
    private String serviceName;
    private String price;
    private String totalPrice;
    private Long totalQuantity;
}
