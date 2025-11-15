package com.airlock.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 일단 CSRF 끄기 (API/WS 테스트용)
                .csrf(csrf -> csrf.disable())

                // 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // WebSocket 엔드포인트는 모두 허용
                        .requestMatchers("/ws/**").permitAll()
                        // 나머지도 일단 전부 허용 (테스트 단계라서)
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
