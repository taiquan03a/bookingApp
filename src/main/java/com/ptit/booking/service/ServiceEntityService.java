package com.ptit.booking.service;

import com.ptit.booking.dto.serviceRoom.RoomSelectRequest;
import com.ptit.booking.dto.serviceRoom.ServiceBookingRequest;
import com.ptit.booking.model.Room;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface ServiceEntityService {
    ResponseEntity<?> getServiceRooms();
    ResponseEntity<?> getServiceBookings(List<RoomSelectRequest> roomSelectRequestList);
    ResponseEntity<?> getServiceDetail(Long serviceId);
    ResponseEntity<?> getCartService(List<ServiceBookingRequest> serviceBookingRequestList);
}
