import React, { useState } from "react";

function ChatInput({ userId, targetUserId, sendSignal }) {
  const [text, setText] = useState("");

  const sendMessage = () => {
    if (!text.trim()) return;

    // 입력된 텍스트 → signaling type으로 사용
    const type = text.trim();

    const signalPayload = {
      type: type,       // offer / answer / candidate / join
      from: userId,
      to: targetUserId,
      text: text        // UI 표시용 (실제 signaling에는 쓰지 않아도 됨)
    };

    // candidate 추가 파라미터는 테스트 시 필요할 수 있어 그대로 둠
    if (type === "offer" || type === "answer") {
      signalPayload.sdp = "<dummy_sdp>";
    }

    if (type === "candidate") {
      signalPayload.candidate = "<dummy_candidate>";
      signalPayload.sdpMid = "0";
      signalPayload.sdpMLineIndex = 0;
    }

    sendSignal(signalPayload);
    setText("");
  };

  return (
    <div className="border-t flex items-center p-3 bg-white">
      <input
        type="text"
        placeholder="join / offer / answer / candidate 입력"
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && sendMessage()}
        className="flex-1 border rounded-full px-4 py-2"
      />

      <button
        onClick={sendMessage}
        className="ml-2 bg-[#DDE2B2] px-4 py-2 rounded-full"
      >
        전송
      </button>
    </div>
  );
}

export default ChatInput;
