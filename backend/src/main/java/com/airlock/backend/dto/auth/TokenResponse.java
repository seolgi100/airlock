package com.airlock.backend.dto.auth;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class TokenResponse {
    private String accessToken;
}