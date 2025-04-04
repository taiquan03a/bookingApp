package com.ptit.booking.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelRequest {
    private Long hotelId;
    private String hotelName;
    private String hotelLocation;
    private float hotelRating;
    private String imageUrl;
    private int sumReview;
    private String promotionName;
    private List<Integer> roomIdList;
    private float price;
}
