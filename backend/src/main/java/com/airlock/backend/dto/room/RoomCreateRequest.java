package com.airlock.backend.dto.room;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RoomCreateRequest {
    private Long userId1;
    private Long userId2;
}