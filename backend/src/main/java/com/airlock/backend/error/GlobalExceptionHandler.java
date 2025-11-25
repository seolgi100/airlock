package com.airlock.backend.error;

import com.airlock.backend.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request
    ) {
        String raw = e.getMessage();
        String code = raw;
        String message = "잘못된 요청입니다.";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        log.warn("Bad request. path={}, message={}", request.getRequestURI(), raw);

        if (raw == null) {
            code = "BAD_REQUEST";
        } else {
            switch (raw) {
                // ===== 400 Bad Request 계열 =====
                case "USERNAME_REQUIRED" -> {
                    message = "아이디를 입력해 주세요.";
                }
                case "STEP_REQUIRED" -> {
                    message = "step 값이 필요합니다.";
                }
                case "INVALID_STEP" -> {
                    message = "유효하지 않은 step 값입니다.";
                }
                case "CHALLENGE_NOT_FOUND" -> {
                    message = "만료되었거나 존재하지 않는 challenge 입니다.";
                }
                case "CREDENTIAL_ID_REQUIRED" -> {
                    message = "credentialId가 필요합니다.";
                }
                case "CLIENT_DATA_REQUIRED" -> {
                    message = "clientDataJSON이 필요합니다.";
                }
                case "AUTHENTICATOR_DATA_REQUIRED" -> {
                    message = "authenticatorData가 필요합니다.";
                }
                case "SIGNATURE_REQUIRED" -> {
                    message = "signature가 필요합니다.";
                }
                case "INVALID_CHALLENGE_TYPE" -> {
                    message = "유효하지 않은 WebAuthn type 입니다.";
                }
                case "INVALID_CHALLENGE" -> {
                    message = "challenge 값이 일치하지 않습니다.";
                }
                case "INVALID_ORIGIN" -> {
                    message = "허용되지 않은 origin 입니다.";
                }
                case "AUTH_DATA_TOO_SHORT" -> {
                    message = "authenticatorData 길이가 너무 짧습니다.";
                }
                case "RPID_HASH_MISMATCH" -> {
                    message = "rpIdHash가 일치하지 않습니다.";
                }
                case "USER_NOT_PRESENT" -> {
                    message = "사용자 존재(User Presence)가 확인되지 않았습니다.";
                }
                case "USER_NOT_VERIFIED" -> {
                    message = "사용자 검증(User Verification)에 실패했습니다.";
                }

                // signCount 롤백은 보안 이슈 → 401/403 중 마음대로, 일단 401
                case "SIGN_COUNT_ROLLBACK" -> {
                    status = HttpStatus.UNAUTHORIZED;
                    message = "인증기 signCount가 감소했습니다. 복제된 인증기 가능성이 있습니다.";
                }

                // ===== 404 Not Found 계열 =====
                case "USER_NOT_FOUND" -> {
                    status = HttpStatus.NOT_FOUND;
                    message = "해당 사용자를 찾을 수 없습니다.";
                }
                case "CREDENTIAL_NOT_FOUND" -> {
                    status = HttpStatus.NOT_FOUND;
                    message = "등록된 패스키를 찾을 수 없습니다.";
                }

                // ===== 401 Unauthorized 계열 =====
                case "TOKEN_REQUIRED" -> {
                    status = HttpStatus.UNAUTHORIZED;
                    message = "인증 토큰이 필요합니다.";
                }
                case "INVALID_TOKEN" -> {
                    status = HttpStatus.UNAUTHORIZED;
                    message = "유효하지 않은 토큰입니다.";
                }
                case "USERNAME_ALREADY_EXISTS" -> {
                    status = HttpStatus.BAD_REQUEST; // 혹은 409 Conflict
                    message = "이미 사용 중인 아이디입니다.";
                }

                default -> {
                    // prefix 기반 처리 (WEBAUTHN_ 계열)
                    if (raw.startsWith("WEBAUTHN_AUTH_FAILED")) {
                        code = "WEBAUTHN_AUTH_FAILED";
                        status = HttpStatus.UNAUTHORIZED;
                        message = "패스키 인증에 실패했습니다. 다시 시도해 주세요.";
                    } else if (raw.startsWith("WEBAUTHN_REGISTER_FAILED")) {
                        code = "WEBAUTHN_REGISTER_FAILED";
                        message = "패스키 등록에 실패했습니다. 다시 시도해 주세요.";
                    } else {
                        code = "BAD_REQUEST";
                        message = "요청을 처리할 수 없습니다.";
                    }
                }
            }
        }

        ErrorResponse body = new ErrorResponse(status.value(), code, message);
        return ResponseEntity.status(status).body(body);
    }

    //JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        log.warn("JSON parse error. path={}, message={}", request.getRequestURI(), e.getMessage());

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_JSON",
                "요청 본문(JSON)을 읽을 수 없습니다."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 예상하지 못한 다른 예외들
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception e, HttpServletRequest request) {
        log.error("Internal server error. path={}", request.getRequestURI(), e);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
