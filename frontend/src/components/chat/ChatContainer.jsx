// src/components/chat/ChatContainer.jsx
import { useState, useEffect, useRef } from "react";
import ChatHeader from "./ChatHeader";
import ChatRoom from "./ChatRoom";
import ChatInput from "./ChatInput";

const API_BASE = process.env.REACT_APP_API_URL;  

let WS_URL;
if (API_BASE.startsWith("https://")) {
  WS_URL = API_BASE.replace("https://", "wss://") + "/ws";
} else if (API_BASE.startsWith("http://")) {
  WS_URL = API_BASE.replace("http://", "ws://") + "/ws";
}


function ChatContainer({ room, currentUser, onBack }) {

  const [messages, setMessages] = useState([]);
  const wsRef = useRef(null);

  const userId = currentUser?.id;
  const targetUserId =
    room?.user1?.id === userId ? room?.user2?.id : room?.user1?.id;

useEffect(() => {
    if (!room || !userId) return;

    console.log(
      "ChatContainer mount. roomId =",
      room.roomId,
      "userId =",
      userId
    );

    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log("WS 연결됨:", WS_URL);

      // -----------------------------------------
      // 2) 서버 스펙에 맞춘 join 메시지
      //    { "type": "join", "from": "userA" }
      // -----------------------------------------
      const joinMsg = {
        type: "join",
        from: userId, // 문자열이어도 상관 없음
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

      // -----------------------------------------
      // 3) 텍스트 채팅: { type:"chat", from:"1", to:"2", message:"..." }
      // -----------------------------------------
      if (msg.type === "chat") {
        setMessages((prev) => [
          ...prev,
          {
            id: msg.id ?? Date.now() + Math.random(),
            text: msg.message ?? "",
            sender: String(msg.from) === String(userId) ? "me" : "other",
          },
        ]);
        return;
      }

      // WebRTC 시그널링 (offer / answer / candidate 등)
      if (
        msg.type === "offer" ||
        msg.type === "answer" ||
        msg.type === "candidate"
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
  }, [room, userId]);

  // -----------------------------------------
  // 4) 공통 전송 함수 (시그널링 / 채팅 모두)
  //    백엔드는 from/to 기반으로 forwardToTarget()만 하기 때문에
  //    반드시 { type, from, to, ... } 형태로 보내야 함
  // -----------------------------------------
  const sendSignal = (payload) => {
    const ws = wsRef.current;

    if (!ws || ws.readyState !== WebSocket.OPEN) {
      console.warn("WS가 아직 열리지 않음");
      return;
    }

    // payload: { type, message?, sdp?, candidate?, sdpMid?, sdpMLineIndex? }
    const finalPayload = {
      ...payload,
      from: userId,
      to: targetUserId,
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
