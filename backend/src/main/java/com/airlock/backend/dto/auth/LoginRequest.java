package com.airlock.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class LoginRequest {

    @Schema(description = "사용자 식별용 이름")
    private String username;

    @Schema(description = "표시 이름")
    private String displayName;

    @Schema(description = "요청 단계 (REGISTER_OPTIONS, REGISTER_VERIFY, AUTH_OPTIONS, AUTH_VERIFY)", example = "REGISTER_OPTIONS")
    private String step;

    @Schema(description = "클라이언트가 돌려주는 challenge(검증 시)")
    private String clientChallenge;

    @Schema(description = "패스키 식별자")
    private String credentialId;

    //WebAuthn
    private String clientDataJSON;      // Base64URL
    private String attestationObject;   // 등록 시
    private String authenticatorData;   // 로그인 시
    private String signature;           // 로그인 시
    private String userHandle;          // 선택적
}
