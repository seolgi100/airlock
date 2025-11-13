package com.airlock.backend.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class LoginRequest {
    private String username;        //사용자 식별용 이름
    private String displayName;     //표시 이름
    private String step;            //요청 단계
    private String clientChallenge; //클라이언트가 돌려주는 challenge(검증 시)
    private String credentialId;    //패스키 식별자
}
