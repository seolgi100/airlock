package com.airlock.backend.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "passkey")
public class PasskeyProperties {
    private String rpId;
    private String origin;

    public void setRpId(String rpId) {
        this.rpId = rpId;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
