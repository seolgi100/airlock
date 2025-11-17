// components/chat/ChatInput.jsx
// 역할: 입력만 담당. axios 제거.
//       부모(ChatContainer)에게 sendMessage(text) 호출만 함.

import React, { useState } from "react";

function ChatInput({ sendMessage }) {
  const [text, setText] = useState("");

  // 메시지 전송 함수
  const submit = () => {
    if (text.trim() === "") return;

    sendMessage(text);  // 부모에게 text 전달
    setText("");        // 입력창 비우기
  };

  return (
    <div className="border-t flex items-center p-3 bg-white">
      <input
        type="text"
        placeholder="메시지를 입력하세요"
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && submit()}
        className="flex-1 border rounded-full px-4 py-2 focus:outline-none focus:ring focus:ring-[#DDE2B2]"
      />

      <button
        onClick={submit}
        className="ml-2 bg-[#DDE2B2] hover:bg-[#cdd59b] px-4 py-2 rounded-full font-semibold"
      >
        전송
      </button>
    </div>
  );
}

export default ChatInput;
