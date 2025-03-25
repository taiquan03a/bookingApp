package com.ptit.booking.service;

import com.ptit.booking.dto.hotel.FilterRequest;
import org.springframework.http.ResponseEntity;

public interface HotelService {
    ResponseEntity<?> home();
    ResponseEntity<?> search(String sortBy, String sort, int page, FilterRequest filterRequest);

}
