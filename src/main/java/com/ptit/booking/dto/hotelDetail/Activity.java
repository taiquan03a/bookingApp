package com.ptit.booking.dto.hotelDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Activity {
    private String name;
    private String description;
    private String distance;
    private float rating;
    private String photoUrl;
    private String latitude;
    private String longitude;
}
