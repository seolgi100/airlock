package com.airlock.backend.controller;

import com.airlock.backend.service.AuthService;
import com.airlock.backend.dto.auth.ChallengeResponse;
import com.airlock.backend.dto.auth.LoginRequest;
import com.airlock.backend.dto.auth.TokenResponse;
import com.airlock.backend.dto.auth.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //Challenge 발급
    @PostMapping("/issue")
    public ResponseEntity<ChallengeResponse> issue(@RequestBody LoginRequest req) {
        ChallengeResponse res = authService.issueChallenge(req);
        return ResponseEntity.ok(res);
    }

    //검증
    @PostMapping("/verify")
    public ResponseEntity<TokenResponse> verify(@RequestBody LoginRequest req) {
        TokenResponse res = authService.verify(req);
        return ResponseEntity.ok(res);
    }

    //사용자 조회
    @GetMapping("/me")
    @Operation(
        summary = "현재 사용자 조회",
        parameters = {
            @Parameter(name = HttpHeaders.AUTHORIZATION, in = ParameterIn.HEADER,
                example = "Bearer dev-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
        }
    )

    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        System.out.println("[DEBUG] Raw Authorization: " + authorization);
        String token = extractBearer(authorization);
        System.out.println("[DEBUG] Extracted token: " + token);
        UserResponse res = authService.me(token);
        return ResponseEntity.ok(res);
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String token = extractBearer(authorization);
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    //토큰 추출
    private String extractBearer(String authorization) {
        if (authorization == null) return null;
        String prefix = "Bearer ";
        return authorization.startsWith(prefix) ? authorization.substring(prefix.length()) : authorization;
    }
}
