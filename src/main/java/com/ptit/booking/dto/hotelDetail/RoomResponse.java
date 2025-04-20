package com.ptit.booking.dto.hotelDetail;

import com.ptit.booking.dto.policy.PolicyRoom;
import com.ptit.booking.dto.promotion.PromotionBookingRoom;
import com.ptit.booking.model.Promotion;
import com.ptit.booking.model.ServiceEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class RoomResponse {
    private Long roomId;
    private String roomName;
    private int area;
    private String bed;
    private Set<ServiceEntity> serviceEntityList;
    private int selectDay;
    private BigDecimal price;
    private BigDecimal promotionPrice;
    private PromotionBookingRoom promotion;
    private int roomQuantity;
    private Set<PolicyRoom> policyRoomList;
}
