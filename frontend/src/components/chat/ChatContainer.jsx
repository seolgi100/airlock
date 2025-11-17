// components/chat/ChatContainer.jsx

import { useState, useEffect, useRef } from "react";
import ChatHeader from "./ChatHeader";
import ChatRoom from "./ChatRoom";
import ChatInput from "./ChatInput";

function ChatContainer() {
  const roomId = 1;
  const userId = "me";
  const targetUserId = "other";

  const [messages, setMessages] = useState([]);
  const wsRef = useRef(null);

  const WS_URL =
    window.location.hostname === "localhost"
      ? "ws://localhost:8080/ws"
      : `ws://${window.location.hostname}:8080/ws`;

  useEffect(() => {
    wsRef.current = new WebSocket(WS_URL);

    wsRef.current.onopen = () => {
      console.log("WS 연결됨");

      wsRef.current.send(
        JSON.stringify({
          type: "join",
          from: userId,
          roomId: roomId,
        })
      );
    };

    wsRef.current.onmessage = (event) => {
      const msg = JSON.parse(event.data);

      // 무조건 ChatRoom에 표시
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now(),
          text: `[${msg.type}] ${msg.from} → ${msg.to || ""} ${msg.text || ""}`,
          sender: msg.from === userId ? "me" : "other",
        },
      ]);
    };

    return () => wsRef.current.close();
  }, [roomId, userId, WS_URL]);

  // ChatInput에서 호출됨
  const sendSignal = (payload) => {
  const ws = wsRef.current;

  // WebSocket이 아직 연결 안 됐으면, 대기
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.warn("WS 아직 연결 안됨. 재시도 중...");

    setTimeout(() => sendSignal(payload), 100); // 0.1초 후 재시도
    return;
  }

  ws.send(JSON.stringify(payload));
};

  return (
    <div className="w-full min-h-[100svh] bg-white md:max-w-screen-md md:mx-auto">
      <div className="flex flex-col min-h-[100svh] pt-[env(safe-area-inset-top)]">

        <header className="border-b bg-white">
          <ChatHeader />
        </header>

        <ChatRoom messages={messages} />

        <footer className="border-t bg-white pb-[env(safe-area-inset-bottom)]">
          <ChatInput
            userId={userId}
            targetUserId={targetUserId}
            sendSignal={sendSignal}
          />
        </footer>

      </div>
    </div>
  );
}

export default ChatContainer;
