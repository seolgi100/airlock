package com.airlock.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DevTokenAuthenticationFilter devTokenAuthenticationFilter)
            throws Exception {

        http
                //API 서버라 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //패스키 발급/검증, 데모 로그인 등은 토큰 없이 허용
                        .requestMatchers("/auth/issue", "/auth/verify", "/auth/demo-login").permitAll()

                        //WebSocket 엔드포인트 모두 허용
                        .requestMatchers("/ws/**").permitAll()

                        //Swagger UI, API 문서 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()
                        
                        // API 허용 추가
                        .requestMatchers("/api/**").permitAll()

                        //그 외 나머지 인증 필요
                        .anyRequest().authenticated()
                )

                .addFilterBefore(devTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트엔드 주소
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://43.202.212.164",
                "http://43.202.212.164:3000",
                "http://43.202.212.164:8080"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));   // 모든 헤더 허용
        config.setAllowCredentials(true);         // 쿠키/인증정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}