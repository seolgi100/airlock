package com.airlock.backend.service;

import com.airlock.backend.config.PasskeyProperties;
import com.airlock.backend.domain.entity.PasskeyCredential;
import com.airlock.backend.domain.repository.PasskeyCredentialRepository;
import com.airlock.backend.dto.auth.ChallengeResponse;
import com.airlock.backend.dto.auth.LoginRequest;
import com.airlock.backend.dto.auth.TokenResponse;
import com.airlock.backend.dto.auth.UserResponse;
import com.airlock.backend.domain.entity.User;
import com.airlock.backend.domain.repository.UserRepository;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.*;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasskeyCredentialRepository passkeyCredentialRepository;
    private final WebAuthnManager webAuthnManager;
    private final PasskeyProperties passkeyProperties = new PasskeyProperties();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String RP_ID = "43.202.212.164";
    private static final String ORIGIN = "http://43.202.212.164:3000";


    //발급 시 저장, 검증 시 일치 확인 후 제거(일회성 저장소) / (username, challenge)
    private final Map<String, String> challengeStore = new ConcurrentHashMap<>();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();

    public AuthService(UserRepository userRepository,
                       TokenService tokenService,
                       PasskeyCredentialRepository passkeyCredentialRepository,
                       WebAuthnManager webAuthnManager) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passkeyCredentialRepository = passkeyCredentialRepository;
        this.webAuthnManager = webAuthnManager;
    }

    //================= 내부 유틸 =================

    //Base64URL로 인코딩해서 challenge 문자열 생성
    private String newChallenge() {
        byte[] challenge = new byte[32];
        SECURE_RANDOM.nextBytes(challenge);
        return B64URL.encodeToString(challenge);
    }

    //Base64URL 디코딩 유틸
    private static byte[] b64url(String v) {
        return Base64.getUrlDecoder().decode(v);
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

        String rpId = RP_ID;
        String origin = ORIGIN;

        if ("REGISTER_OPTIONS".equalsIgnoreCase(req.getStep())) {
            //회원가입: allowCredentials 필요 없음
            return ChallengeResponse.builder()
                    .challenge(challenge)
                    .rpId(rpId)
                    .origin(origin)
                    .credentialIds(null)
                    .build();

        } else if ("AUTH_OPTIONS".equalsIgnoreCase(req.getStep())) {
            //로그인: 유저의 passkey 목록을 allowCredentials로 내려줌
            User user = userRepository.findByUsername(req.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

            List<String> credentialIds = passkeyCredentialRepository.findByUser(user)
                    .stream()
                    .map(PasskeyCredential::getCredentialId)
                    .toList();

            return ChallengeResponse.builder()
                    .challenge(challenge)
                    .rpId(rpId)
                    .origin(origin)
                    .credentialIds(credentialIds)
                    .build();
        } else {
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

        if ("REGISTER_VERIFY".equalsIgnoreCase(req.getStep())) {
            return handleRegisterVerify(req);
        } else if ("AUTH_VERIFY".equalsIgnoreCase(req.getStep())) {
            return handleAuthVerify(req);
        } else {
            throw new IllegalArgumentException("INVALID_STEP");
        }
    }

    private TokenResponse handleRegisterVerify(LoginRequest req) {
        String expectedChallenge = challengeStore.get(req.getUsername());
        if (expectedChallenge == null) {
            throw new IllegalArgumentException("CHALLENGE_NOT_FOUND");
        }

        try {
            //WebAuthn4J용 RegistrationRequest 만들기
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    b64url(req.getAttestationObject()),   // attestationObject
                    b64url(req.getClientDataJSON())       // clientDataJSON
            );

            //ServerProperty 구성 (RP, Origin, Challenge)
            ServerProperty serverProperty = new ServerProperty(
                    new Origin(ORIGIN),
                    RP_ID,
                    new DefaultChallenge(b64url(expectedChallenge)),
                    null
            );

            RegistrationParameters parameters = new RegistrationParameters(
                    serverProperty,
                    null,
                    false
            );

            //검증
            RegistrationData registrationData =
                    webAuthnManager.validate(registrationRequest, parameters);

            challengeStore.remove(req.getUsername());

            if (userRepository.existsByUsername(req.getUsername())) {
                throw new IllegalArgumentException("USERNAME_ALREADY_EXISTS");
            }
            User user = new User();
            user.setUsername(req.getUsername());
            String dn = (req.getDisplayName() == null || req.getDisplayName().isBlank())
                    ? req.getUsername()
                    : req.getDisplayName();
            user.setDisplayName(dn);
            user = userRepository.save(user);

            PasskeyCredential cred = new PasskeyCredential();
            cred.setUser(user);
            cred.setCredentialId(req.getCredentialId());

            var attestedCredentialData = registrationData.getAttestationObject()
                    .getAuthenticatorData()
                    .getAttestedCredentialData();

            cred.setPublicKeyCose(
                    Base64.getUrlEncoder().withoutPadding()
                            .encodeToString(
                                    attestedCredentialData
                                            .getCOSEKey()
                                            .getPublicKey()
                                            .getEncoded()
                            )
            );
            cred.setSignCount(
                    registrationData.getAttestationObject()
                            .getAuthenticatorData()
                            .getSignCount()
            );
            passkeyCredentialRepository.save(cred);

            String token = tokenService.issueToken(user.getId());
            return new TokenResponse(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("WEBAUTHN_REGISTER_FAILED");
        }
    }

    private TokenResponse handleAuthVerify(LoginRequest req) {
        //기본 입력 검증
        if (req.getCredentialId() == null || req.getCredentialId().isBlank()) {
            throw new IllegalArgumentException("CREDENTIAL_ID_REQUIRED");
        }
        if (req.getClientDataJSON() == null || req.getClientDataJSON().isBlank()) {
            throw new IllegalArgumentException("CLIENT_DATA_REQUIRED");
        }
        if (req.getAuthenticatorData() == null || req.getAuthenticatorData().isBlank()) {
            throw new IllegalArgumentException("AUTHENTICATOR_DATA_REQUIRED");
        }
        if (req.getSignature() == null || req.getSignature().isBlank()) {
            throw new IllegalArgumentException("SIGNATURE_REQUIRED");
        }

        //서버가 발급했던 challenge 가져오기
        String expectedChallenge = challengeStore.get(req.getUsername());
        if (expectedChallenge == null) {
            throw new IllegalArgumentException("CHALLENGE_NOT_FOUND");
        }

        try {
            //------clientDataJson 파싱------
            byte[] clientDataBytes = b64url(req.getClientDataJSON());
            JsonNode clientDate = OBJECT_MAPPER.readTree(clientDataBytes);

            String type = clientDate.get("type").asText();
            String challengeFromClient = clientDate.get("challenge").asText();
            String originFromClient = clientDate.get("origin").asText();

            //type 확인
            if (!"webauthn.get".equals(type)) {
                throw new IllegalArgumentException("INVALID_CHALLENGE_TYPE");
            }

            //challenge 확인
            if (!expectedChallenge.equals(challengeFromClient)) {
                throw new IllegalArgumentException("INVALID_CHALLENGE");
            }

            //origin 확인
            if (!ORIGIN.equals(originFromClient)) {
                throw new IllegalArgumentException("INVALID_ORIGIN");
            }

            //------authenticatorData 파싱------
            byte[] authDataBytes = b64url(req.getAuthenticatorData());

            if (authDataBytes.length < 37) { //rpIdHash(32) + flags(1) + signCount(4)
                throw new IllegalArgumentException("AUTH_DATA_TOO_SHORT");
            }

            //rpIdHash 체크
            byte[] rpIdHash = Arrays.copyOfRange(authDataBytes, 0, 32);
            byte[] expectedRpIdHash = MessageDigest.getInstance("SHA-256")
                    .digest(RP_ID.getBytes(StandardCharsets.UTF_8));

            if (!MessageDigest.isEqual(rpIdHash, expectedRpIdHash)) {
                throw new IllegalArgumentException("RPID_HASH_MISMATCH");
            }

            //flags
            byte flags = authDataBytes[32];
            boolean userPresent = (flags & 0x01) != 0;
            boolean userVerified = (flags & 0x04) != 0;

            if (!userPresent) {
                throw new IllegalArgumentException("USER_NOT_PRESENT");
            }

            if (!userVerified) {
                throw new IllegalArgumentException("USER_NOT_VERIFIED");
            }

            long newSignCount =
                    ((authDataBytes[33] & 0xffL) << 24) |
                    ((authDataBytes[34] & 0xffL) << 16) |
                    ((authDataBytes[35] & 0xffL) << 8)  |
                    (authDataBytes[36] & 0xffL);

            //-----DB에서 credential / user 조회------
            PasskeyCredential cred = passkeyCredentialRepository.findByCredentialId(req.getCredentialId())
                    .orElseThrow(() -> new IllegalArgumentException("CREDENTIAL_NOT_FOUND"));

            User user = cred.getUser();

            //-----서명 검증-----
            byte[] clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataBytes);

            byte[] signedBytes = new byte[authDataBytes.length + clientDataHash.length];
            System.arraycopy(authDataBytes, 0, signedBytes, 0, authDataBytes.length);
            System.arraycopy(clientDataHash, 0, signedBytes, authDataBytes.length, clientDataHash.length);

            //저장해둔 publicKeyCose는 X.509 인코딩된 공개키를 Base64URL로 저장했다고 가정
            byte[] publicKeyBytes = Base64.getUrlDecoder().decode(cred.getPublicKeyCose());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);

            //ES256 기준
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            byte[] signatureBytes = b64url(req.getSignature());

            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(signedBytes);

            if (!sig.verify(signatureBytes)) {
                throw new IllegalArgumentException("INVALID_SIGNATURE");
            }

            //-----signCount 롤백 체크 및 업데이트-----
            Long storedSignCount = cred.getSignCount();
            if (storedSignCount != null && newSignCount < storedSignCount) {
                //복제된 인증기 사용 가능성
                throw new IllegalArgumentException("SIGN_COUNT_ROLLBACK");
            }

            cred.setSignCount(newSignCount);
            passkeyCredentialRepository.save(cred);

            //-----challenge 사용 후 제거-----
            challengeStore.remove(req.getUsername());

            String token = tokenService.issueToken(user.getId());
            return new TokenResponse(token);

        } catch (Exception e) {
            throw new IllegalArgumentException("WEBAUTHN_AUTH_FAILED");
        }
    }

    // ===================== 현재 사용자 조회 =====================

    //전달받은 토큰 유효성 검사 / 사용자의 정보 찾아서 반환
    @Transactional(readOnly = true)
    public UserResponse me(String bearerToken) {
        //입력 검증
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("TOKEN_REQUIRED");
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

    // ===================== username으로 유저 조회 =====================
    @Transactional(readOnly = true)
    public UserResponse meByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("USERNAME_REQUIRED");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName());
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