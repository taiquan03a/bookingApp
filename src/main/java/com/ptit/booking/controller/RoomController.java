package com.ptit.booking.controller;


import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/room")
public class RoomController {
    private final RoomService roomService;
    @GetMapping("/select_room")
    public ResponseEntity<?> selectRoom(@RequestBody SelectRoomRequest selectRoomRequest){
        return roomService.selectRooms(selectRoomRequest);
    }
}
