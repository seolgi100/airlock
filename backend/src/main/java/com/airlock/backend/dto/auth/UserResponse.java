package com.airlock.backend.dto.auth;

import com.airlock.backend.domain.entity.User;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String displayName;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName()
        );
    }
}
