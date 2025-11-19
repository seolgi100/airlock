package com.airlock.backend.dto.room;

import com.airlock.backend.domain.entity.Room;
import com.airlock.backend.dto.auth.UserResponse;
import lombok.Getter;

import java.time.Instant;

@Getter
public class RoomResponse {

    private final Long roomId;
    private final UserResponse user1;
    private final UserResponse user2;
    private final Instant createdAt;

    public RoomResponse(Long roomId, UserResponse user1, UserResponse user2,
                        Instant createdAt) {
        this.roomId = roomId;
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
    }

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                UserResponse.from(room.getUser1()),
                UserResponse.from(room.getUser2()),
                room.getCreatedAt()
        );
    }
}
