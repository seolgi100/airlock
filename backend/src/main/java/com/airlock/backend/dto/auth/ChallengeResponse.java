package com.airlock.backend.dto.auth;

import lombok.*;

import java.util.List;

//WebAuthn Challenge 응답
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChallengeResponse {

    //Base64URL 인코딩된 challenge
    private String challenge;

    //허용되는 자격 목록
    private List<Object> allowCredentials;
}
