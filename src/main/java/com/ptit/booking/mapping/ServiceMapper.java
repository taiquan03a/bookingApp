package com.ptit.booking.mapping;


import com.ptit.booking.dto.room.RoomChoseService;
import com.ptit.booking.dto.serviceRoom.ServiceRoomSelect;
import com.ptit.booking.model.Room;
import com.ptit.booking.model.ServiceEntity;
import com.ptit.booking.model.ServiceRoom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    @Mapping(source = "serviceRooms", target = "roomChoseServiceList", qualifiedByName = "mapRooms")
    @Mapping(source = "price", target = "price", qualifiedByName = "formatPrice")
    ServiceRoomSelect toServiceRoomSelect(ServiceEntity entity);
    List<ServiceRoomSelect> toServiceRoomSelectList(List<ServiceEntity> entities);

    @Named("mapRooms")
    static List<RoomChoseService> mapRooms(Set<ServiceRoom> serviceRooms) {
        if (serviceRooms == null) return List.of();

        return serviceRooms.stream()
                .map(sr -> {
                    Room room = sr.getRoom();
                    return new RoomChoseService(room.getId(), room.getName());
                })
                .collect(Collectors.toList());
    }

    @Named("formatPrice")
    static String formatPrice(BigDecimal price) {
        if (price == null) return "0.00";
        return new DecimalFormat("#,###.00").format(price);
    }
}