import React, { useEffect, useState } from "react";
import axios from "axios";
import ChatContainer from "./components/chat/ChatContainer";
import ChatList from "./components/chat/ChatList";

function App() {
  const [hello, setHello] = useState("");
  const [selectedRoom, setSelectedRoom] = useState(null);

  useEffect(() => {
    axios
      .get("/hello")
      .then((res) => setHello(res.data))
      .catch(() => setHello("서버 연결 실패"));
  }, []);

  return (
    <div className="min-h-[100svh] bg-gray-100">

      {/* 채팅방 리스트 */}
      {!selectedRoom && (
        <ChatList onSelectRoom={(roomId) => setSelectedRoom(roomId)} />
      )}

      {/* 채팅방 들어오면 ChatContainer로 변경 */}
      {selectedRoom && (
        <ChatContainer roomId={selectedRoom} serverHello={hello} />
      )}
    </div>
  );
}

export default App;
