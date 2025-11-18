import React, { useEffect, useState } from "react";
import ChatListItem from "./ChatListItem";
import axios from "axios";
import api from "../../api";

function ChatList({ onSelectRoom, onOpenLogin }) {
  const [rooms, setRooms] = useState([]);

  useEffect(() => {
    api
      .get("/api/chat/rooms")
      .then((res) => setRooms(res.data))
      .catch(() => {
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

      {/* 헤더 영역: 채팅 목록 + 로그인 버튼 */}
      <div className="flex items-center justify-between p-4 border-b bg-white">
        <h1 className="text-xl font-bold">채팅 목록</h1>

        <button
          onClick={onOpenLogin}
          className="bg-[#F2F0E5] px-4 py-2 rounded-lg border hover:bg-[#DDE2B2]"
        >
          로그인
        </button>
      </div>

      {/* 방 목록 리스트 */}
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
