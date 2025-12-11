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
    public RoomResponse createRoom(
            @RequestBody RoomCreateRequest request,
            Authentication authentication
    ) {
        String username = authentication.getPrincipal().toString();
        User me = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("ME_NOT_FOUND"));

        String targetUserName = request.getTargetUserName();

        User target = userRepository.findByUsername(targetUserName)
                .orElseThrow(() -> new IllegalArgumentException("TARGET_NOT_FOUND"));

        return roomService.createOrGetRoom(me.getId(), target.getId());
    }

    // 2) 방 단일 조회
    @GetMapping("/{roomId}")
    public RoomResponse getRoom(@PathVariable Long roomId) {
        return roomService.getRoom(roomId);
    }

    // 3) 유저의 방 목록 조회
    @GetMapping
    public List<RoomResponse> getMyRooms(Authentication authentication) {
        //로그인 안되어 있으면 빈 리스트 리턴
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        String username = (String) authentication.getPrincipal();

        //유저 정보 없으면 빈 리스트 리턴
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return List.of();
        }
        return roomService.getRoomsForUser(user.getId());
    }
}
