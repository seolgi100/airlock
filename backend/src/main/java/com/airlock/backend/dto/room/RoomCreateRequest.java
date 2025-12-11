package com.airlock.backend.dto.room;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RoomCreateRequest {
    private String targetUserName;
}