// RegisterModal: 회원가입 모달창을 렌더링하는 React 함수형 컴포넌트
// props로 onClose(닫기 함수), onSwitchToLogin(로그인 창으로 전환 함수)를 받음

import React, { useState } from "react";
import ModalWrapper from "./ModalWrapper";

export default function RegisterModal({ onClose, onSwitchToLogin = () => {} }) {
  // useState 훅을 사용해 에러 메시지 상태 관리
  const [error, setError] = useState("");

  // 폼 제출 시 실행되는 함수
  const handleSubmit = (e) => {
    e.preventDefault(); // 기본 새로고침 방지

    // 입력값 가져오기
    const username = e.target.username.value;
    const password = e.target.password.value;
    const confirm = e.target.confirm.value;

    // 비밀번호 불일치 검사
    if (password !== confirm) {
      setError("비밀번호가 일치하지 않습니다."); // 에러 메시지 출력
      return;
    }

    // 통과 시 에러 초기화
    setError("");
    alert(`회원가입 성공!\n사용자 이름: ${username}`); // 임시 알림 (백엔드 연결 전 테스트용)
    onClose(); // 모달 닫기
  };

  return (
    // ModalWrapper: 모달 레이아웃(제목, 닫기 버튼 등)을 감싸는 공용 컴포넌트
    <ModalWrapper title="회원가입" onClose={onClose}>
      {/* 회원가입 입력 폼 */}
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {/* 사용자 이름 입력 */}
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

        {/* 비밀번호 입력 */}
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

        {/* 비밀번호 확인 입력 */}
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

        {/* 오류 메시지 출력 영역 */}
        {error && (
          <p className="text-red-500 text-sm font-medium text-center -mt-2">
            {error}
          </p>
        )}

        {/* 회원가입 버튼: 로그인 모달과 동일 톤 */}
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
          회원가입
        </button>
      </form>

      {/* 로그인으로 전환 링크 */}
      <div className="text-center mt-4 text-sm">
        이미 계정이 있으신가요?{" "}
        <button
          onClick={onSwitchToLogin} // 로그인 모달로 전환
          className="text-[#5a6146] hover:underline"
        >
          로그인
        </button>
      </div>
    </ModalWrapper>
  );
}
