import React, { useState, useEffect } from "react";
import ChatContainer from "./components/chat/ChatContainer";
import ChatList from "./components/chat/ChatList";

// 모달들
import LoginModal from "./components/modal/LoginModal";
import RegisterModal from "./components/modal/RegisterModal";

import apiClient from "./api/client";

function App() {
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);  

  useEffect(() => {
    const token = localStorage.getItem("accessToken");

    if (!token) return;

    apiClient
      .get("/auth/validate")
      .then((res) => {
        // res.data = { id, username, displayName }
        setCurrentUser(res.data);
      })
      .catch((err) => {
        console.error("토큰 검증 실패:", err);
        localStorage.removeItem("accessToken");
        setCurrentUser(null);
      });
  }, []);

    const handleLogout = async () => {
    const token = localStorage.getItem("accessToken");

    try {
      if (token) {
        await apiClient.post(
          "/auth/logout",
          {},
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
      }
    } catch (err) {
      console.error("로그아웃 실패(무시 가능):", err);
    } finally {
      // 토큰/유저 정보 비우기
      localStorage.removeItem("accessToken");
      setCurrentUser(null);
      setSelectedRoom(null);
    }
  };
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
          onLogout={handleLogout}          // ★ 추가
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

// 테스트