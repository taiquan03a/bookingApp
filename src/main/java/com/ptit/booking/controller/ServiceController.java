package com.ptit.booking.controller;

import com.ptit.booking.dto.serviceRoom.RoomSelectRequest;
import com.ptit.booking.dto.serviceRoom.ServiceBookingRequest;
import com.ptit.booking.service.ServiceEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/service")
public class ServiceController {
    private final ServiceEntityService serviceEntityService;

    @GetMapping("get_list")
    public ResponseEntity<?> getList() {
        return serviceEntityService.getServiceRooms();
    }
    @PostMapping("get_by_category")
    public ResponseEntity<?> getByCategory(@RequestBody List<RoomSelectRequest> roomSelectRequestList) {
        return serviceEntityService.getServiceBookings(roomSelectRequestList);
    }
    @GetMapping("get_detail/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        return serviceEntityService.getServiceDetail(id);
    }
    @PostMapping("get_cart")
    public ResponseEntity<?> getCart(@RequestBody List<ServiceBookingRequest> serviceBookingRequestList) {
        return serviceEntityService.getCartService(serviceBookingRequestList);
    }

}
