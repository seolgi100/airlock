package com.airlock.backend.dto.auth;

import lombok.*;

import java.util.List;

//WebAuthn Challenge 응답
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChallengeResponse {

    private String challenge;       //Base64URL 인코딩된 challenge
    private String rpId;            //15.165.2.31(도메인)
    private String origin;          //http://15.165.2.31:3000
    private List<String> credentialIds; //로그인 시 allowCredentials 구성용
}
