package com.airlock.backend.domain.repository;

import com.airlock.backend.domain.entity.Room;
import com.airlock.backend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByUser1AndUser2(User user1, User user2);

    //특정 유저가 속한 모든 방 조회
    List<Room> findByUser1OrUser2(User user1, User user2);
}