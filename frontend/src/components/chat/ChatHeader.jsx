// ChatHeader: 채팅창 상단의 헤더 영역을 렌더링하는 React 함수형 컴포넌트
// 역할: 채팅방 이름 또는 서비스 타이틀 + 왼쪽 뒤로가기 버튼
// props:
//   - onBack: 채팅방 목록으로 돌아갈 때 호출할 함수 (선택)

import React from "react";

function ChatHeader({ onBack }) {
  return (
    <div className="bg-[#F2F0E5] text-gray-800 border-b">
      <div className="flex items-center">
        {/* 왼쪽: 뒤로가기 버튼 */}
        {onBack && (
          <button
            type="button"
            onClick={onBack}
            className="px-3 py-2 text-xl font-bold"
          >
            {"<"}
          </button>
        )}

        {/* 가운데: 타이틀 */}
        <div className="flex-1 text-center font-semibold py-3">
          AirLock Chat
        </div>

        {/* 오른쪽 공간 맞추기용 (버튼 폭만큼) */}
        {onBack && <div className="w-[3rem]" />}
      </div>
    </div>
  );
}

export default ChatHeader;
