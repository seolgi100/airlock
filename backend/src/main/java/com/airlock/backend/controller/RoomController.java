package com.airlock.backend.controller;

import com.airlock.backend.domain.entity.User;
import com.airlock.backend.domain.repository.UserRepository;
import com.airlock.backend.dto.room.RoomCreateRequest;
import com.airlock.backend.dto.room.RoomResponse;
import com.airlock.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final UserRepository userRepository;

    // 1) 방 생성
    @PostMapping
    public RoomResponse createRoom(@RequestBody RoomCreateRequest request) {
        return roomService.createOrGetRoom(request);
    }

    // 2) 방 단일 조회
    @GetMapping("/{roomId}")
    public RoomResponse getRoom(@PathVariable Long roomId) {
        return roomService.getRoom(roomId);
    }

    // 3) 유저의 방 목록 조회
    @GetMapping
    public List<RoomResponse> getMyRooms(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("AUTH_REQUIRED");
        }

        String username = (String) authentication.getPrincipal();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        return roomService.getRoomsForUser(user.getId());
    }
}
