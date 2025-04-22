package com.ptit.booking.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Information {
    private String avatar;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
