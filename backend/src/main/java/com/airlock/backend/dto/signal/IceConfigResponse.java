package com.airlock.backend.dto.signal;

import lombok.Data;

import java.util.List;

@Data
public class IceConfigResponse {
    private List<IceServerDto> iceServers;

    public IceConfigResponse(List<IceServerDto> iceServerDtos) {
        this.iceServers = iceServerDtos;
    }
}

