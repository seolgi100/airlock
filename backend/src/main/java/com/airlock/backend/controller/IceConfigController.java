package com.airlock.backend.controller;

import com.airlock.backend.dto.signal.IceServerDto;
import com.airlock.backend.dto.signal.IceConfigResponse;
import com.airlock.backend.config.TurnProperties;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController @AllArgsConstructor
public class IceConfigController {

    private final TurnProperties turnProperties;

    @GetMapping("/ice-servers")
    public IceConfigResponse getIceServers() {
        return new IceConfigResponse(
                List.of(
                        new IceServerDto("stun:" + turnProperties.getIp() + ":" + turnProperties.getPort()),
                        new IceServerDto(
                                "turn:" + turnProperties.getIp() + ":" + turnProperties.getPort() + "?transport=udp",
                                turnProperties.getUsername(), turnProperties.getPassword())
                )
        );
    }
}
