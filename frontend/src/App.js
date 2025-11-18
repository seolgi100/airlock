import React, { useEffect, useState } from "react";
import ChatContainer from "./components/chat/ChatContainer";
import ChatList from "./components/chat/ChatList";
import api from "./api";

// 모달들
import LoginModal from "./components/modal/LoginModal";
import RegisterModal from "./components/modal/RegisterModal";

function App() {
  const [hello, setHello] = useState("");
  const [selectedRoom, setSelectedRoom] = useState(null);

  // 로그인 / 회원가입 모달 상태
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);

  // /hello 테스트용
  useEffect(() => {
    api
      .get("/hello")
      .then((res) => setHello(res.data))
      .catch(() => setHello("서버 연결 실패"));
  }, []);

  return (
    <div className="min-h-[100svh] bg-gray-100">
      {/* 로그인 모달 */}
      {showLogin && (
        <LoginModal
          onClose={() => setShowLogin(false)}
          onSwitchToRegister={() => {
            setShowLogin(false);
            setShowRegister(true);
          }}
        />
      )}

      {/* 회원가입 모달 */}
      {showRegister && (
        <RegisterModal
          onClose={() => setShowRegister(false)}
          onSwitchToLogin={() => {
            setShowRegister(false);
            setShowLogin(true);
          }}
        />
      )}

      {/* 채팅방 리스트 화면 */}
      {!selectedRoom && (
        <ChatList
          onSelectRoom={(roomId) => setSelectedRoom(roomId)}
          onOpenLogin={() => setShowLogin(true)} // 헤더의 "로그인" 버튼
        />
      )}

      {/* 채팅방 입장 화면 */}
      {selectedRoom && (
        <ChatContainer roomId={selectedRoom} serverHello={hello} />
      )}
    </div>
  );
}

export default App;
