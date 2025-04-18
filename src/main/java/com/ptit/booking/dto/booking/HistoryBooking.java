package com.ptit.booking.dto.booking;

import com.ptit.booking.dto.hotel.HotelHistoryResponse;
import com.ptit.booking.enums.EnumBookingStatus;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class HistoryBooking {
    private String bookingStatus;
    private List<HotelHistoryResponse> hotelBookingList;
}
