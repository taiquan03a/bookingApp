package com.ptit.booking.controller;

import com.ptit.booking.service.ServiceEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("service")
public class ServiceController {
    private final ServiceEntityService serviceEntityService;

    @GetMapping("get_list")
    public ResponseEntity<?> getList() {
        return serviceEntityService.getServiceRooms();
    }

}
