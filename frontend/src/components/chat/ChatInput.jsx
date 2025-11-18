import React, { useState } from "react";

function ChatInput({ userId, targetUserId, sendSignal }) {
  const [text, setText] = useState("");

  const handleSend = () => {
    if (!text.trim()) return;

    const payload = {
      type: text,           // 사용자 입력 = 시그널링 타입
      from: userId,
      to: targetUserId,
      text: text,           // 메시지 내용도 표시
    };

    sendSignal(payload);
    setText("");
  };

  return (
    <div className="border-t flex items-center p-3 bg-white">
      <input
        type="text"
        placeholder="메세지를 입력해 주세요."
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && handleSend()}
        className="flex-1 border rounded-full px-4 py-2"
      />

      <button
        onClick={handleSend}
        className="ml-2 bg-[#DDE2B2] px-4 py-2 rounded-full"
      >
        전송
      </button>
    </div>
  );
}

export default ChatInput;
