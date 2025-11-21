import React from "react";

function ChatListItem({ room, onSelect }) {
  return (
    <div
      onClick={onSelect}
      className="p-4 hover:bg-gray-50 cursor-pointer flex justify-between"
    >
      <div>
        <div className="font-semibold text-gray-800">
          채팅방 #{room.roomId}
        </div>

        <div className="text-sm text-gray-500">
          {room.user1?.displayName || room.user1?.username} ↔{" "}
          {room.user2?.displayName || room.user2?.username}
        </div>
      </div>

      <div className="text-xs text-gray-400">
        {room.createdAt ? new Date(room.createdAt).toLocaleString() : ""}
      </div>
    </div>
  );
}

export default ChatListItem;
