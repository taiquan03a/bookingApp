package com.ptit.booking.dto.serviceRoom;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ServiceBookingCart {
    private List<ServiceBookingResponse> serviceBookingList;
    private List<PriceService> priceServiceList;
    private String totalPrice;
}
