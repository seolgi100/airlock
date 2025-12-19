import { useEffect, useState, useCallback  } from "react";
import apiClient from "../../api/client";
import ChatListItem from "./ChatListItem";
import ChatRoomCreate from "./ChatRoomCreate";

function ChatList({ currentUser, onSelectRoom, onOpenLogin, onLogout })  {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

const userId = currentUser?.id ?? null;

const fetchRooms = useCallback(async () => {
    if (!userId) {
      setRooms([]);
      setLoading(false);
      setLoadError(null);
      return;
    }

    try {
      setLoading(true);
      setLoadError(null);

      console.log("GET /api/rooms params =", { userId });
      const res = await apiClient.get("/api/rooms", {
        params: { userId },
      });

      const data = Array.isArray(res.data) ? res.data : [];
      setRooms(data);
    } catch (err) {
      console.error("방 목록 불러오기 실패:", err);
      setLoadError("채팅방 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [userId]);


  useEffect(() => {
    fetchRooms();
  }, [fetchRooms]);

  return (
    <div className="w-full min-h-[100svh] bg-gray-100">
      <div className="flex items-center justify-between p-4 border-b bg-white">
  <h1 className="text-xl font-bold">채팅 목록</h1>

  <div className="flex items-center gap-2">
    {currentUser ? (
      <>
        <span className="text-sm text-gray-700">
          {currentUser.displayName || currentUser.username} 님
        </span>
        <button
          type="button"
          className="
            text-sm text-gray-600 
            border border-gray-300 
            rounded-lg px-3 py-1 
            hover:bg-gray-50 
            transition
          "
          onClick={onLogout}
        >
          로그아웃
        </button>
      </>
    ) : (
      <button
        type="button"
        className="
          text-sm text-gray-600 
          border border-gray-300 
          rounded-lg px-3 py-1 
          hover:bg-gray-50 
          transition
        "
        onClick={onOpenLogin}
      >
        로그인
      </button>
    )}
  </div>
</div>

      <div className="p-4 space-y-2">
        {loading && <div className="text-sm text-gray-500">불러오는 중…</div>}
        {loadError && (
          <div className="text-sm text-red-500">{loadError}</div>
        )}
        {!loading && !loadError && rooms.length === 0 && (
          <div className="text-sm text-gray-500">채팅방이 없습니다.</div>
        )}
        {rooms.map((room) => (
          <ChatListItem
            key={room.roomId}
            room={room}
            onSelect={() => onSelectRoom(room)}
          />
        ))}
      </div>
        <ChatRoomCreate
        onCreated={fetchRooms}
        onRequireLogin={!currentUser ? onOpenLogin : null}
      />
    </div>
  );
}

export default ChatList;


// test deploy