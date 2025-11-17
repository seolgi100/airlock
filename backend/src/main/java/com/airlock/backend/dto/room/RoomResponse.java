package com.airlock.backend.dto.room;

import com.airlock.backend.domain.entity.Room;
import com.airlock.backend.domain.entity.User;
import lombok.Getter;

import java.time.Instant;

@Getter
public class RoomResponse {

    private final Long roomId;
    private final Long userId1;
    private final Long userId2;
    private final Instant createdAt;

    public RoomResponse(Long roomId, Long userId1, Long userId2, Instant createdAt) {
        this.roomId = roomId;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.createdAt = createdAt;
    }

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getUser1().getId(),
                room.getUser2().getId(),
                room.getCreatedAt()
        );
    }
}
