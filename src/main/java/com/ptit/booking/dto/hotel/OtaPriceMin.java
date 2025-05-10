package com.ptit.booking.dto.hotel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtaPriceMin {
    private String otaName;
    private String minPrice;
}
