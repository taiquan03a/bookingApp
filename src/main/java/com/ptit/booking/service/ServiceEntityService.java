package com.ptit.booking.service;

import com.ptit.booking.model.Room;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface ServiceEntityService {
    ResponseEntity<?> getServiceRooms();
    ResponseEntity<?> getServiceBookings(List<Long> roomIdList);
}
