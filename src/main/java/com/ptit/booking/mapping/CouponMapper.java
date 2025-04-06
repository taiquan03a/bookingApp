package com.ptit.booking.mapping;

import com.ptit.booking.dto.coupon.CouponDto;
import com.ptit.booking.model.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    CouponMapper INSTANCE = Mappers.getMapper(CouponMapper.class);
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "discountValue", target = "discountValue"),
            @Mapping(source = "minBookingAmount", target = "minBookingAmount"),
            @Mapping(source = "expiryDate", target = "expirationDate"),
            @Mapping(source = "validFromDate", target = "validFromDate"),
            @Mapping(source = "description", target = "description"),
            @Mapping(source = "code", target = "code")
    })
    CouponDto toDto(Coupon coupon);
    List<CouponDto> toDtoList(List<Coupon> coupons);
}

