// components/chat/ChatContainer.jsx

import { useState, useEffect, useRef } from "react";
import ChatHeader from "./ChatHeader";
import ChatRoom from "./ChatRoom";
import ChatInput from "./ChatInput";

function ChatContainer() {
  const roomId = 1;
  const userId = "me"; // 로그인 유저라고 가정

  // 메시지를 저장하는 상태
  const [messages, setMessages] = useState([]);

  // WebSocket을 저장하는 참조 (컴포넌트가 재렌더되어도 유지됨)
  const wsRef = useRef(null);

  // 로컬/배포 자동 분기
  const WS_URL =
    window.location.hostname === "localhost"
      ? "ws://localhost:8080/ws"
      : `ws://${window.location.hostname}:8080/ws`;

  // WebSocket 연결을 부모에서 단 한 번만 생성
  useEffect(() => {
    wsRef.current = new WebSocket(WS_URL);

    wsRef.current.onopen = () => {
      console.log("WS 연결됨");

      // 서버에 JOIN 알림 (방 입장)
      wsRef.current.send(
        JSON.stringify({
          type: "join",
          from: userId,
          roomId: roomId,
        })
      );
    };

    // 서버에서 메시지를 받으면 messages 배열에 추가
    wsRef.current.onmessage = (event) => {
      const msg = JSON.parse(event.data);

      if (msg.type === "message") {
        setMessages((prev) => [
          ...prev,
          {
            id: Date.now(), // 임시 ID
            text: msg.text,
            sender: msg.from === userId ? "me" : "other",
          },
        ]);
      }
    };

    wsRef.current.onclose = () => {
      console.log("WS 연결 종료");
    };

    // 컴포넌트 종료 시 WebSocket 닫기
    return () => wsRef.current.close();
  }, [roomId, userId]);

  // ChatInput에서 호출하는 전송 기능
  const sendMessage = (text) => {
    const payload = {
      type: "message",
      from: userId,
      roomId: roomId,
      text: text,
    };

    // 서버로 WebSocket 메시지 전송
    wsRef.current.send(JSON.stringify(payload));
  };

  return (
    <div className="w-full min-h-[100svh] bg-white md:max-w-screen-md md:mx-auto">
      <div className="flex flex-col min-h-[100svh] pt-[env(safe-area-inset-top)]">

        {/* 상단 헤더 */}
        <header className="border-b bg-white">
          <ChatHeader />
        </header>

        {/* 메시지 출력 → 부모의 messages 전달 */}
        <ChatRoom messages={messages} />

        {/* 메시지 입력 → 부모의 sendMessage 전달 */}
        <footer className="border-t bg-white pb-[env(safe-area-inset-bottom)]">
          <ChatInput sendMessage={sendMessage} />
        </footer>

      </div>
    </div>
  );
}

export default ChatContainer;
