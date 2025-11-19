package com.airlock.backend.service;


import com.airlock.backend.domain.entity.Room;
import com.airlock.backend.domain.entity.User;
import com.airlock.backend.domain.repository.RoomRepository;
import com.airlock.backend.domain.repository.UserRepository;
import com.airlock.backend.dto.room.RoomCreateRequest;
import com.airlock.backend.dto.room.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    //두 유저 사이에 방 하나만 유지(있으면 재사용, 없으면 생성)
    @Transactional
    public RoomResponse createOrGetRoom(RoomCreateRequest request) {
        Long userId1 = request.getUserId1();
        Long userId2 = request.getUserId2();

        if(userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("USER_IDS_REQUIRED");
        }
        if(userId1.equals(userId2)) {
            throw new IllegalArgumentException("CANNOT_CREATE_ROOM_WITH_SAME_USER");
        }

        //실제 User 엔티티 조회
        User u1 = userRepository.findById(userId1)
                .orElseThrow(() -> new IllegalArgumentException("USER1_NOT_FOUND"));
        User u2 = userRepository.findById(userId2)
                .orElseThrow(() -> new IllegalArgumentException("USER2_NOT_FOUND"));

        //항상 작은 쪽이 user1, 큰 쪽은 user2로 저장
        //중복 방 생성 막기
        User first = u1.getId() < u2.getId() ? u1 : u2;
        User second = (first == u1) ? u2 : u1;

        return roomRepository.findByUser1AndUser2(first, second)
                .map(RoomResponse::from)
                .orElseGet(()-> {
                   Room room = new Room(first, second);
                   Room saved = roomRepository.save(room);
                   return RoomResponse.from(saved);
                });
    }

    //방 ID로 조회
    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ROOM_NOT_FOUND"));
        return RoomResponse.from(room);
    }

    //특정 유저가 속한 모든 방 조회
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        return roomRepository.findByUser1OrUser2(user, user)
                .stream()
                .map(RoomResponse::from)
                .toList();
    }
}
