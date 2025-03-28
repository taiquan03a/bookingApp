package com.ptit.booking.service;

import com.ptit.booking.dto.hotel.FilterRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface HotelService {
    ResponseEntity<?> home();
    ResponseEntity<?> search(String sortBy, String sort, int page, FilterRequest filterRequest);
    //List<Map<String, Object>> searchPlaces(String location);
    ResponseEntity<?> hotelDetail(Long hotelId);
    ResponseEntity<?> selectRooms (Long hotelId);
}
