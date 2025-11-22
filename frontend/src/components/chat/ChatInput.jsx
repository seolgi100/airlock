// src/components/chat/ChatInput.jsx
import { useState } from "react";

function ChatInput({ userId, targetUserId, sendSignal }) {
  const [text, setText] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    const trimmed = text.trim();
    if (!trimmed) return;

    sendSignal({
      type: "chat",
      to: targetUserId,
      message: trimmed,
    });

    setText("");
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex items-center gap-2 px-4 py-3 bg-white"
    >
      <input
        type="text"
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="메시지를 입력해 주세요."
        className="flex-1 border rounded-full px-4 py-2 text-sm outline-none"
      />
      <button
        type="submit"
        className="px-4 py-2 text-sm font-medium rounded-full bg-[#F2F0E5] border border-gray-300"
      >
        전송
      </button>
    </form>
  );
}

export default ChatInput;
