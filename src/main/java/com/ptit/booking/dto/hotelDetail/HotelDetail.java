package com.ptit.booking.dto.hotelDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelDetail {
    private ReviewDto review;
    private List<String> images;
    private NearBy nearBy;
    private float priceMin;
    private float priceNoPromotion;
}
