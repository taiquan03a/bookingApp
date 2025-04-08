package com.ptit.booking.mapping;


import com.ptit.booking.dto.serviceRoom.ServiceRoomSelect;
import com.ptit.booking.model.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    @Mapping(source = "image", target = "image")
    @Mapping(source = "price", target = "price", qualifiedByName = "formatPrice")
    ServiceRoomSelect toDto(ServiceEntity entity);

    // Nếu bạn muốn map danh sách:
    List<ServiceRoomSelect> toDtoList(List<ServiceEntity> entities);

    @org.mapstruct.Named("formatPrice")
    static String formatPrice(BigDecimal price) {
        if (price == null) return "0.00";
        return new DecimalFormat("#,###.00").format(price);
    }
}
