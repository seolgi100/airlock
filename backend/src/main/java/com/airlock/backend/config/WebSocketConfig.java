package com.airlock.backend.config;

import com.airlock.backend.controller.SignalingHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration @EnableWebSocket @RequiredArgsConstructor @Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final SignalingHandler signalingHandler;

    @PostConstruct
    public void init() {
        log.info("WebSocketConfig initialized, handler = {}", signalingHandler.getClass().getName());
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingHandler, "/ws")
                .setAllowedOriginPatterns("*");
    }
}
