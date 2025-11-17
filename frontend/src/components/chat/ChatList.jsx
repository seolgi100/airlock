// components/chat/ChatList.jsx
import React, { useEffect, useState } from "react";
import ChatListItem from "./ChatListItem";
import axios from "axios";

function ChatList({ onSelectRoom }) {
  const [rooms, setRooms] = useState([]);

  useEffect(() => {
    // 실제 서버 연동 시 바꾸면 됨
    axios
      .get("/api/chat/rooms")
      .then((res) => setRooms(res.data))
      .catch(() => {
        // 임시 더미 데이터
        setRooms([
          {
            id: 1,
            name: "개발자1",
            lastMessage: "오늘 코딩하자...",
            time: "오전 10:23",
          },
          {
            id: 2,
            name: "개발자2",
            lastMessage: "아이고",
            time: "어제",
          },
        ]);
      });
  }, []);

  return (
    <div className="w-full min-h-[100svh] bg-gray-100">
      <h1 className="text-xl font-bold p-4 border-b bg-white">채팅 목록</h1>

      <div className="divide-y bg-white">
        {rooms.map((room) => (
          <ChatListItem
            key={room.id}
            room={room}
            onSelect={() => onSelectRoom(room.id)}
          />
        ))}
      </div>
    </div>
  );
}

export default ChatList;
