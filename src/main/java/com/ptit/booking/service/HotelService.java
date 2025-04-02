package com.ptit.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HotelService {
    ResponseEntity<?> home(Principal principal);
    ResponseEntity<?> search(String sortBy, String sort, int page, FilterRequest filterRequest, Principal principal) throws JsonProcessingException;
    //List<Map<String, Object>> searchPlaces(String location);
    ResponseEntity<?> hotelDetail(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate);
    ResponseEntity<?> selectRooms (SelectRoomRequest selectRoomRequest);
}
