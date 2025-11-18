import React from "react";
import ModalWrapper from "./ModalWrapper";

export default function LoginModal({ onClose, onSwitchToRegister = () => {} }) {
  return (
    <ModalWrapper title="로그인" onClose={onClose}>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          const username = e.target.username.value;
          const password = e.target.password.value;
          alert(`로그인 시도: ${username} / ${password}`); // 백엔드 붙기 전 테스트용
        }}
        className="flex flex-col gap-4"
      >
        {/* 사용자 이름 */}
        <input
          type="text"
          name="username"
          placeholder="사용자 이름 입력"
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
          placeholder="비밀번호 입력"
          className="
            border border-gray-300 
            rounded-lg px-4 py-2 
            focus:ring-2 focus:ring-[#DDE2B2] focus:border-[#DDE2B2]
            transition
          "
          required
        />

        {/* 로그인 버튼: 채팅 UI 색과 통일 */}
        <button
          type="submit"
          className="
            bg-[#F2F0E5] 
            text-gray-800 
            py-2 rounded-lg 
            border border-gray-300
            hover:bg-[#DDE2B2] 
            transition
          "
        >
          로그인
        </button>
      </form>

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
