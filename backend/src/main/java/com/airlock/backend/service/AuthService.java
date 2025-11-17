package com.airlock.backend.service;

import com.airlock.backend.dto.auth.ChallengeResponse;
import com.airlock.backend.dto.auth.LoginRequest;
import com.airlock.backend.dto.auth.TokenResponse;
import com.airlock.backend.dto.auth.UserResponse;
import com.airlock.backend.domain.entity.User;
import com.airlock.backend.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
//import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    //발급 시 저장, 검증 시 일치 확인 후 제거(일회성 저장소) / (username, challenge)
    private final Map<String, String> challengeStore = new ConcurrentHashMap<>();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    //================= 내부 유틸 =================

    //Base64URL로 인코딩해서 challenge 문자열 생성
    private String newChallenge() {
        byte[] challenge = new byte[32];
        SECURE_RANDOM.nextBytes(challenge);
        return B64URL.encodeToString(challenge);
    }

    //================= 발급 =================

    //사용자에게 challenge 발급해주는 메소드
    public ChallengeResponse issueChallenge(LoginRequest req) {
        //입력 검증
        if (req == null || req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("USERNAME_REQUIRED");
        }
        if (req.getStep() == null) {
            throw new IllegalArgumentException("STEP_REQUIRED");
        }

        //새로운 challenge 생성 후 username에 매핑 저장
        String challenge = newChallenge();
        challengeStore.put(req.getUsername(), challenge);

        //단계별 처리
        if ("AUTH_OPTIONS".equalsIgnoreCase(req.getStep())) {
            //로그인 단계
            //allowCredentials: 실제 구현 시엔 DB에 저장된 credentialId 목록을 내려줌
            return new ChallengeResponse(challenge, /*allowCredentials=*/null);

        } else if ("REGISTER_OPTIONS".equalsIgnoreCase(req.getStep())) {
            //회원가입 단계
            return new ChallengeResponse(challenge, /*allowCredentials=*/null);

        } else {
            //잘못된 단계
            throw new IllegalArgumentException("INVALID_STEP");
        }
    }

    // ===================== 검증 =====================

    //사용자가 보낸 challenge 응답을 검증해서 사용자를 인증하고, 성공하면 세션 토큰 발급
    @Transactional
    public TokenResponse verify(LoginRequest req) {
        //입력 검증
        if (req == null || req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("USERNAME_REQUIRED");
        }
        if (req.getStep() == null || req.getStep().isBlank()) {
            throw new IllegalArgumentException("STEP_REQUIRED");
        }
        if (req.getClientChallenge() == null || req.getClientChallenge().isBlank()) {
            throw new IllegalArgumentException("CHALLENGE_REQUIRED");
        }

        //challenge 일치 확인
        String expected = challengeStore.get(req.getUsername());
        if (expected == null || !expected.equals(req.getClientChallenge())) {
            throw new IllegalArgumentException("INVALID_CHALLENGE" + req.getClientChallenge());
        }

        //사용 후 제거(일회성)
        challengeStore.remove(req.getUsername());

        User user;

        //단계별 처리
        if ("REGISTER_VERIFY".equalsIgnoreCase(req.getStep())) {
            //회원가입 단계

            //username 중복 체크
            if (userRepository.existsByUsername(req.getUsername())) {
                throw new IllegalArgumentException("USERNAME_ALREADY_EXISTS");
            }

            //새 유저 생성
            User u = new User();
            u.setUsername(req.getUsername());

            String dn = (req.getDisplayName() == null || req.getDisplayName().isBlank())
                    ? req.getUsername()
                    : req.getDisplayName();
            u.setDisplayName(dn);

            user = userRepository.save(u);

            //(선택) credentialId 등은 실제 구현에서 별도 테이블에 저장
            // String credentialId = req.getCredentialId();
        } else if ("AUTH_VERIFY".equalsIgnoreCase(req.getStep())) {
            //로그인 단계
            //인증 검증: 유저 반드시 존재
            user = userRepository.findByUsername(req.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        } else {
            //잘못된 단계
            throw new IllegalArgumentException("INVALID_STEP");
        }

        //토큰 발급 및 반환
        String token = tokenService.issueToken(user.getId());
        return new TokenResponse(token);
    }

    // ===================== 현재 사용자 조회 =====================

    //전달받은 토큰 유효성 검사 / 사용자의 정보 찾아서 반환
    @Transactional(readOnly = true)
    public UserResponse me(String bearerToken) {
        //입력 검증
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("TOKEN_REQUIRED" + bearerToken);
        }

        Long userId = tokenService.resolveUserId(bearerToken);

        if (userId == null) {
            //유효하지 않은 토큰
            throw new IllegalArgumentException("INVALID_TOKEN");
        }

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName());
    }

    // ===================== 로그아웃 =====================

    public void logout(String bearerToken) {

        if (bearerToken != null && !bearerToken.isBlank()) {
            tokenService.revoke(bearerToken);
        }
    }

    // ===================== 데모용 =====================
    @Transactional
    public TokenResponse demoLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User(username, username);
                    u.setActive(true);
                    return userRepository.save(u);
                });

        String token = tokenService.issueToken(user.getId());

        return new TokenResponse(token);
    }
}
