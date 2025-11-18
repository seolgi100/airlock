import { useState, useEffect, useRef } from "react";
import ChatHeader from "./ChatHeader";
import ChatRoom from "./ChatRoom";
import ChatInput from "./ChatInput";

function ChatContainer({ roomId, serverHello }) {
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
          roomId,
        })
      );
    };

    wsRef.current.onmessage = (event) => {
      const msg = JSON.parse(event.data);

      setMessages((prev) => [
        ...prev,
        {
          id: Date.now(),
          text: msg.text ?? "",
          sender: msg.from === userId ? "me" : "other",
        },
      ]);
    };

    return () => wsRef.current.close();
  }, [roomId]);

  // 메시지 전송
  const sendSignal = (payload) => {
    const ws = wsRef.current;

    if (!ws || ws.readyState !== WebSocket.OPEN) {
      console.warn("WS 연결 중... 재시도");
      setTimeout(() => sendSignal(payload), 100);
      return;
    }

    ws.send(JSON.stringify(payload));
  };

  return (
    <div className="w-full min-h-[100svh] bg-white md:max-w-screen-md md:mx-auto">
      <div className="flex flex-col min-h-[100svh]">

        {/* 헤더 */}
        <header className="border-b bg-white">
          <ChatHeader roomId={roomId} serverHello={serverHello} />
        </header>

        {/* 메시지 리스트 */}
        <ChatRoom messages={messages} />

        {/* 입력창 */}
        <footer className="border-t bg-white">
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
