package com.ptit.booking.dto.serviceRoom;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ptit.booking.model.ServiceEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceBooked {
    private Long serviceId;
    private String serviceName;
    private String serviceType;
    private String image;
    private String description;
    private Long quantity;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM-yyyy HH:mm:ss",timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime time;
    private String note;
    private String price;
    private String priceBooked;
}
