// src/components/chat/ChatContainer.jsx
import { useState, useEffect, useRef } from "react";
import ChatHeader from "./ChatHeader";
import ChatRoom from "./ChatRoom";
import ChatInput from "./ChatInput";

const WS_URL =
  window.location.hostname === "localhost"
    ? "ws://localhost:8080/ws"
    : "ws://15.165.2.31:8080/ws";
    
function ChatContainer({ roomId, onBack }) {
  // test1 / test2 유저 id (DB id 기준)
  // 브라우저 1에서 1, 브라우저 2에서 2로만 바꿔서 테스트
  const userId = 1;
  const targetUserId = userId === 1 ? 2 : 1;

  const [messages, setMessages] = useState([]);
  const wsRef = useRef(null);

  useEffect(() => {
    console.log("ChatContainer mount. roomId =", roomId);

    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log("WS 연결됨:", WS_URL);

      // 스웨거 스펙에 맞춘 join 메시지
      const joinMsg = {
        type: "join",
        from: userId,
        to: null,
        sdp: null,
        candidate: null,
        sdpMid: null,
        sdpMLineIndex: null,
      };


      ws.send(JSON.stringify(joinMsg));
      console.log("WS 전송(join):", joinMsg);
    };

    ws.onmessage = (event) => {
      console.log("WS 수신(raw):", event.data);

      let msg;
      try {
        msg = JSON.parse(event.data);
      } catch (e) {
        console.error("WS 메시지 JSON 파싱 실패:", event.data, e);
        return;
      }

      // 텍스트 채팅
      if (msg.type === "chat") {
        setMessages((prev) => [
          ...prev,
          {
            id: msg.id ?? Date.now() + Math.random(),
            text: msg.message ?? "",
            sender: msg.from === userId ? "me" : "other",
          },
        ]);
        return;
      }

      // WebRTC 시그널링 (offer / answer / candidate 등)
      if (
        msg.type === "offer" ||
        msg.type === "answer" ||
        msg.type === "candidate" ||
        msg.type === "join"
      ) {
        console.log("시그널링 메시지:", msg);
        // 나중에 RTCPeerConnection 붙일 때 여기에서 처리
        return;
      }

      console.log("알 수 없는 메시지 타입:", msg);
    };

    ws.onerror = (err) => {
      console.error("WS 오류:", err);
    };

    ws.onclose = () => {
      console.log("WS 연결 종료");
    };

    return () => {
      console.log("ChatContainer unmount. WS 닫기");
      if (wsRef.current) {
        wsRef.current.close();
        wsRef.current = null;
      }
    };
  }, [roomId, userId]);

  // 공통 전송 함수 (시그널링 / 채팅 모두)
  const sendSignal = (payload) => {
    const ws = wsRef.current;

    if (!ws || ws.readyState !== WebSocket.OPEN) {
      console.warn("WS가 아직 열리지 않아서 메시지를 보낼 수 없음");
      return;
    }

    if (!payload || typeof payload !== "object") {
      console.warn("잘못된 payload:", payload);
      return;
    }

    const finalPayload = {
      from: userId,
      ...payload,
    };

    ws.send(JSON.stringify(finalPayload));
    console.log("WS 전송:", finalPayload);

    // 내가 보낸 채팅은 로컬에 바로 반영
    if (finalPayload.type === "chat") {
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + Math.random(),
          text: finalPayload.message ?? "",
          sender: "me",
        },
      ]);
    }
  };

  return (
    <div className="w-full min-h-[100svh] bg-white md:max-w-screen-md md:mx-auto">
      <div className="flex flex-col min-h-[100svh]">
        <header className="border-b bg-white">
          <ChatHeader onBack={onBack} />
        </header>

        <ChatRoom messages={messages} />

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
