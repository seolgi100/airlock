package com.airlock.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data @Component @ConfigurationProperties(prefix = "webrtc.turn")
public class TurnProperties {
    private String ip;
    private String username;
    private String password;
    private int port;
}
