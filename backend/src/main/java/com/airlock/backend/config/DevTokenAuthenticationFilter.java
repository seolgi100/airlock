package com.airlock.backend.config;

import com.airlock.backend.domain.repository.UserRepository;
import com.airlock.backend.service.TokenService;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class DevTokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    public final UserRepository userRepository;

    public DevTokenAuthenticationFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        //다른 필터에서 인증 됐으면 건드리지 않음
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        //토큰이 없으면 그냥 다음 필터로 넘김
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            Long userId = tokenService.resolveUserId(token);

            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.getUsername(), null, authorities
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }

            //정상적으로 끝나면 다음 필터로
            filterChain.doFilter(request, response);

        } catch (Exception e) {

            log.error("Dev token authentication failed. uri={}", request.getRequestURI(), e);

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "INVALID TOKEN");
            return;
        }
    }
}
