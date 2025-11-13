package com.airlock.backend.dto.auth;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String displayName;
}
