package com.ptit.booking.dto.serviceRoom;

import com.ptit.booking.model.ServiceEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceBooked {
    private Long serviceId;
    private String serviceName;
    private String serviceType;
    private String image;
    private String description;
    private Long quantity;
    private String price;
    private String priceBooked;
}
