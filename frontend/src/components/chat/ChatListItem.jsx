// components/chat/ChatListItem.jsx
import React from "react";

function ChatListItem({ room, onSelect }) {
  return (
    <div
      onClick={onSelect}
      className="p-4 hover:bg-gray-50 cursor-pointer flex justify-between"
    >
      <div>
        <div className="font-semibold text-gray-800">{room.name}</div>
        <div className="text-sm text-gray-500">{room.lastMessage}</div>
      </div>
      <div className="text-xs text-gray-400">{room.time}</div>
    </div>
  );
}

export default ChatListItem;
