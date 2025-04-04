package com.ptit.booking.dto.hotel;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class HomeDto {
    private List<SearchHistory> historySearchList;
    private List<HotelRequest> hotelRequestList;
}
