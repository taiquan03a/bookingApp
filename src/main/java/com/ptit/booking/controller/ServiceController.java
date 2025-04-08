package com.ptit.booking.controller;

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
    @GetMapping("get_by_category")
    public ResponseEntity<?> getByCategory(@RequestParam List<Long> roomIdList) {
        return serviceEntityService.getServiceBookings(roomIdList);
    }

}
