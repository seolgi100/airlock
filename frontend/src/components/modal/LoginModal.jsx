import React, { useState } from "react";
import ModalWrapper from "./ModalWrapper";
import apiClient from "../../api/client";


// ArrayBuffer -> base64url
function bufferToBase64Url(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = "";
  bytes.forEach((b) => (binary += String.fromCharCode(b)));
  const base64 = window.btoa(binary);
  return base64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

// base64url -> ArrayBuffer
function base64UrlToBuffer(base64url) {
  const base64 = base64url.replace(/-/g, "+").replace(/_/g, "/");
  const pad = "=".repeat((4 - (base64.length % 4)) % 4);
  const base64Padded = base64 + pad;
  const binary = window.atob(base64Padded);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

export default function LoginModal({
  onClose,
  onSwitchToRegister = () => {},
  onLoginSuccess = () => {},
}) {
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
  e.preventDefault();
  setError("");
  

  if (!window.PublicKeyCredential) {
    setError("이 브라우저는 패스키(WebAuthn)를 지원하지 않습니다.");
    return;
  }

  try {
    setLoading(true);

      // 1) 로그인 옵션 요청: /auth/issue
      const issueBody = {
        displayName: "",         
        step: "LOGIN_OPTIONS",
        clientChallenge: "",
        credentialId: "",
        clientDataJSON: "",
        attestationObject: "",
        authenticatorData: "",
        signature: "",
        userHandle: "",
      };

      const issueRes = await apiClient.post("/auth/issue", issueBody);
      console.log("login issueRes.data:", issueRes.data);

      const { challenge, rpId, credentialIds } = issueRes.data || {};
      if (!challenge) {
        setError("서버에서 challenge를 받지 못했습니다.");
        setLoading(false);
        return;
      }

      const currentHost = window.location.hostname;
      const rpIdForWebAuthn =
        currentHost === "localhost"
          ? "localhost"
          : rpId || currentHost;
          
      // 2) WebAuthn 로그인 옵션 구성
      const publicKey = {
        challenge: base64UrlToBuffer(challenge),

        // rpId: 서버에서 내려준 값이 제일 우선
        rpId: rpIdForWebAuthn,

        // 서버가 credentialIds 내려주면 allowCredentials로 제한
        allowCredentials: Array.isArray(credentialIds)
          ? credentialIds.map((id) => ({
              type: "public-key",
              id: base64UrlToBuffer(id),
            }))
          : undefined,

        userVerification: "preferred",
        timeout: 60000,
      };

      const assertion = await navigator.credentials.get({ publicKey });
      if (!assertion) {
        throw new Error("자격 증명 가져오기에 실패했습니다.");
      }

      const { id: credentialId, response } = assertion;
      const clientDataJSON = bufferToBase64Url(response.clientDataJSON);
      const authenticatorData = bufferToBase64Url(response.authenticatorData);
      const signature = bufferToBase64Url(response.signature);
      const userHandle = response.userHandle
        ? bufferToBase64Url(response.userHandle)
        : "";

      // 3) 로그인 검증: /auth/verify
      const verifyBody = {
        displayName: "",
        step: "LOGIN_VERIFY",
        clientChallenge: challenge,

        credentialId,
        clientDataJSON,
        attestationObject: "",
        authenticatorData,
        signature,
        userHandle,
      };

      const verifyRes = await apiClient.post("/auth/verify", verifyBody);
      console.log("login verifyRes.data:", verifyRes.data);

      const { accessToken } = verifyRes.data || {};
      if (!accessToken) {
        throw new Error("accessToken을 받지 못했습니다.");
      }

      // 4) 토큰 저장
      localStorage.setItem("accessToken", accessToken);

      // 5) 필요하면 /auth/me로 유저 정보 조회 (swagger 예시대로)
      let user = null;
      try {
        const meRes = await apiClient.get("/auth/me", {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });
        user = meRes.data;
        console.log("/auth/me 응답:", user);
      } catch (meErr) {
        console.warn("/auth/me 호출 실패(무시 가능):", meErr);
      }

      // 상위(App)로 로그인 성공 알리기
      onLoginSuccess(user);
      onClose();
    } catch (err) {
      console.error("패스키 로그인 실패:", err);
      setError(
        err.response?.data?.message ||
          err.message ||
          "로그인 중 오류가 발생했습니다."
      );
    } finally {
      setLoading(false);
    }
  };
  return (
    <ModalWrapper title="로그인" onClose={onClose}>
      
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {/* 로그인 버튼: 채팅 UI 색과 통일 */}
        <button
          type="submit"
          disabled={loading}  // ← loading 값 사용
          className="
            bg-[#F2F0E5] 
            text-gray-800 
            py-2 rounded-lg 
            border border-gray-300
            hover:bg-[#DDE2B2] 
            transition
            disabled:opacity-60
            disabled:cursor-not-allowed
          "
        >
          {loading ? "로그인 중..." : "로그인"}  {/* ← loading 값 사용 */}
        </button>
      </form>

      {/* 에러 메시지 */}
      {error && (
        <p className="text-red-500 text-sm font-medium text-center mt-2">
          {error}
        </p>
      )}
      
      {/* 회원가입 */}
      <div className="text-center mt-4 text-sm">
        계정이 없으신가요?{" "}
        <button
          onClick={onSwitchToRegister}
          className="text-[#5a6146] hover:underline"
        >
          회원가입
        </button>
      </div>
    </ModalWrapper>
  );
}
