import React, { useState } from "react";
import ChatContainer from "./components/chat/ChatContainer";
import ChatList from "./components/chat/ChatList";

// 모달들
import LoginModal from "./components/modal/LoginModal";
import RegisterModal from "./components/modal/RegisterModal";

function App() {
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);  

    // 로그인 / 회원가입 모달 상태
  const [showLogin, setShowLogin] = useState(false);
  const [showRegister, setShowRegister] = useState(false);

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
          onLoginSuccess={(user) => {
          setCurrentUser(user);
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
          currentUser={currentUser} 
          onSelectRoom={(room) => setSelectedRoom(room)}
          onOpenLogin={() => setShowLogin(true)}
        />
      )}

      {/* 채팅방 입장 화면 */}
      {selectedRoom && (
        <ChatContainer
          room={selectedRoom}
          currentUser={currentUser}
          onBack={() => setSelectedRoom(null)}
        />
      )}
    </div>
  );
}

export default App;
