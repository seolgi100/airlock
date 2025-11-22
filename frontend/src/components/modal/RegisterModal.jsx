// src/components/auth/RegisterModal.jsx

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

export default function RegisterModal({ onClose, onSwitchToLogin = () => {} }) {
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const username = e.target.username.value.trim();
    const displayName = e.target.displayName.value.trim();
    const password = e.target.password.value;
    const confirm = e.target.confirm.value;

    // 1) 기본 검증 (비밀번호 확인 포함)
    if (!username || !displayName) {
      setError("아이디와 이름을 모두 입력해 주세요.");
      return;
    }
    if (!password || !confirm) {
      setError("비밀번호와 비밀번호 확인을 입력해 주세요.");
      return;
    }
    if (password.length < 8) {
      setError("비밀번호는 8자 이상이어야 합니다.");
      return;
    }
    if (password !== confirm) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    if (!window.PublicKeyCredential) {
      setError("이 브라우저는 패스키(WebAuthn)를 지원하지 않습니다.");
      return;
    }

    try {
      setLoading(true);

      // 2) /auth/issue 호출해서 challenge 받아오기
      //    Request body 형식은 스웨거 DTO에 맞춰줌
      const issueBody = {
        username,
        displayName,
        step: "REGISTER_OPTIONS",
        clientChallenge: "",
        credentialId: "",
        clientDataJSON: "",
        attestationObject: "",
        authenticatorData: "",
        signature: "",
        userHandle: "",
        // password는 DTO에 없지만, 서버에서 받아주면 여기서 같이 보낼 수 있음
        // password,
      };

      const issueRes = await apiClient.post("/auth/issue", issueBody);

      console.log("issueRes.data:", issueRes.data);

      const { challenge, rpId } = issueRes.data || {};

      if (!challenge) {
        setError("서버에서 challenge를 받지 못했습니다.");
        setLoading(false);
        return;
      }

      // 3) WebAuthn용 publicKey 옵션 구성
      const publicKey = {
        challenge: base64UrlToBuffer(challenge),

        rp: {
          id: rpId || window.location.hostname,
          name: "Airlock",
        },

        user: {
          id: new TextEncoder().encode(username), // 유저 고유 ID
          name: username,
          displayName,
        },

        pubKeyCredParams: [
          { type: "public-key", alg: -7 },   // ES256
          { type: "public-key", alg: -257 }, // RS256
        ],

        authenticatorSelection: {
          userVerification: "preferred",
        },

        timeout: 60000,
        attestation: "none",
      };

      // 4) 패스키 생성
      const credential = await navigator.credentials.create({ publicKey });

      if (!credential) {
        throw new Error("자격 증명 생성에 실패했습니다.");
      }

      const { id: credentialId, response } = credential;
      const clientDataJSON = bufferToBase64Url(response.clientDataJSON);
      const attestationObject = bufferToBase64Url(response.attestationObject);

      // 5) /auth/verify 로 검증 요청 (스웨거 DTO에 맞춰서)
      const verifyBody = {
        username,
        displayName,
        step: "REGISTER_VERIFY",   // 예시. 서버에서 실제로 뭘 기대하는지는 팀이랑 맞춰야 함
        clientChallenge: challenge, // 서버가 준 challenge 다시 보냄

        credentialId,
        clientDataJSON,
        attestationObject,

        // 등록 단계라서 assertion 쪽 값은 일단 비워 둠
        authenticatorData: "",
        signature: "",
        userHandle: "",

        // 비밀번호 기반 JWT까지 같이 관리하려면,
        // 여기서 password도 넘기고 서버 DTO에도 추가해야 함.
        // password,
      };

      const verifyRes = await apiClient.post("/auth/verify", verifyBody);

      console.log("verifyRes.data:", verifyRes.data);

      const { accessToken } = verifyRes.data || {};

      if (accessToken) {
        // JWT 저장 (이제부터 인증 필요한 API 호출에 사용)
        localStorage.setItem("accessToken", accessToken);
      }

      alert("회원가입이 완료되었습니다.");
      onClose();
      onSwitchToLogin();
    } catch (err) {
      console.error(err);
      setError(
        err.response?.data?.message ||
          err.message ||
          "회원가입 중 오류가 발생했습니다. 다시 시도해 주세요."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <ModalWrapper title="회원가입" onClose={onClose}>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {/* 아이디 */}
        <input
          type="text"
          name="username"
          placeholder="아이디"
          className="
            border border-gray-300 
            rounded-lg px-4 py-2 
            focus:ring-2 focus:ring-[#DDE2B2] focus:border-[#DDE2B2]
            transition
          "
          required
        />

        {/* 이름/닉네임 */}
        <input
          type="text"
          name="displayName"
          placeholder="이름 또는 닉네임"
          className="
            border border-gray-300 
            rounded-lg px-4 py-2 
            focus:ring-2 focus:ring-[#DDE2B2] focus:border-[#DDE2B2]
            transition
          "
          required
        />

        {/* 비밀번호 */}
        <input
          type="password"
          name="password"
          placeholder="비밀번호 (8자 이상)"
          className="
            border border-gray-300 
            rounded-lg px-4 py-2 
            focus:ring-2 focus:ring-[#DDE2B2] focus:border-[#DDE2B2]
            transition
          "
          required
        />

        {/* 비밀번호 확인 */}
        <input
          type="password"
          name="confirm"
          placeholder="비밀번호 확인"
          className="
            border border-gray-300 
            rounded-lg px-4 py-2 
            focus:ring-2 focus:ring-[#DDE2B2] focus:border-[#DDE2B2]
            transition
          "
          required
        />

        {error && (
          <p className="text-red-500 text-sm font-medium text-center -mt-2">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={loading}
          className="
            bg-[#F2F0E5] 
            text-gray-800 
            py-2 rounded-lg 
            border border-gray-300
            hover:bg-[#DDE2B2] 
            transition
            disabled:opacity-60
          "
        >
          {loading ? "회원가입 진행 중..." : "회원가입"}
        </button>
      </form>

      <div className="text-center mt-4 text-sm">
        이미 계정이 있으신가요?{" "}
        <button
          onClick={onSwitchToLogin}
          className="text-[#5a6146] hover:underline"
        >
          로그인
        </button>
      </div>
    </ModalWrapper>
  );
}
