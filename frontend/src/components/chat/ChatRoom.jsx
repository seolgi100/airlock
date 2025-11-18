// components/chat/ChatRoom.jsx
import React from "react";

function ChatRoom({ messages }) {
  return (
    <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50">
      {messages.map((msg) => (
        <div
          key={msg.id}
          className={`flex ${
            msg.sender === "me" ? "justify-end" : "justify-start"
          }`}
        >
          <div
            className={`px-3 py-2 rounded-lg max-w-[75%] text-gray-900 ${
              msg.sender === "me"
                ? "bg-[#F2F0E5] text-right"
                : "bg-[#DDE2B2] text-left"
            }`}
          >
            {msg.text}
          </div>
        </div>
      ))}
    </div>
  );
}

export default ChatRoom;
