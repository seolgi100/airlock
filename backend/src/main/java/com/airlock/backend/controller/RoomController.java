package com.airlock.backend.controller;

import com.airlock.backend.domain.entity.User;
import com.airlock.backend.dto.room.RoomCreateRequest;
import com.airlock.backend.dto.room.RoomResponse;
import com.airlock.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/rooms") @RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    //방 생성 or 기존 방 리턴
    @PostMapping
    public RoomResponse createRoom(@RequestBody RoomCreateRequest request) {
        return roomService.createOrGetRoom(request);
    }

    //방 조회
    public RoomResponse getRoom(@PathVariable Long roomId) {
        return roomService.getRoom(roomId);
    }

    @GetMapping
    public List<RoomResponse> getRoomsByUser(@RequestParam Long userId) {
        return roomService.getRoomsForUser(userId);
    }
}
