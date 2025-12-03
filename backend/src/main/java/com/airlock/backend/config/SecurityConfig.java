package com.airlock.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DevTokenAuthenticationFilter devTokenAuthenticationFilter
    ) throws Exception {

        http
                //API 서버라 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //패스키 발급/검증, 데모 로그인 등은 토큰 없이 허용
                        .requestMatchers("/auth/issue", "/auth/verify", "/auth/demo-login").permitAll()
                        .requestMatchers("/auth/**").permitAll()

                        //WebSocket 엔드포인트 모두 허용
                        .requestMatchers("/ws/**").permitAll()

                        //Swagger UI, API 문서 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()

                        //방 목록/방 생성 API 허용
                        .requestMatchers("/api/**").permitAll()
                        //그 외 나머지 인증 필요
                        .anyRequest().permitAll()
                )

                .addFilterBefore(debugFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(devTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트엔드 주소
        config.setAllowedOrigins(List.of(
                "http://localhost",
                "http://localhost:3000",
                "http://localhost:8080",
                "http://43.202.212.164",
                "http://43.202.212.164:3000",
                "http://43.202.212.164:8080",
                "https://mychatapp.mooo.com",
                "https://mychatapp.mooo.com/"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));   // 모든 헤더 허용
        config.setAllowCredentials(true);         // 쿠키/인증정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public Filter debugFilter() {
        return new Filter() {

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {

                HttpServletRequest req = (HttpServletRequest) request;
                System.out.println("💡 Received path = " + req.getRequestURI());
                System.out.println("💡 Full URL      = " + req.getRequestURL());

                chain.doFilter(request, response);
            }
        };
    }

}