package com.ptit.booking.dto.serviceRoom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class ServiceBookingRequest {
    @Schema(description = "id dịch vụ phòng", example = "30", defaultValue = "29")
    private Long serviceId;
    @Schema(description = "Danh sách các phòng đã chọn của dịch vụ")
    private List<RoomBookingRequest> roomBookingRequestList;
}

