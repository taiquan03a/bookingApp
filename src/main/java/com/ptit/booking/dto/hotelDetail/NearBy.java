package com.ptit.booking.dto.hotelDetail;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class NearBy {
    private String descriptionLocation;
    private String ratingLocation;
    private List<Activity> activityList;
}
