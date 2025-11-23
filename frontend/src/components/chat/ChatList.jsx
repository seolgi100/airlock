import { useEffect, useState } from "react";
import apiClient from "../../api/client";
import ChatListItem from "./ChatListItem";

function ChatList({ currentUser, onSelectRoom, onOpenLogin, onOpenUserInfo }) {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

const userId = currentUser?.id ?? null;

  useEffect(() => {
    let cancelled = false;

     // userId 없으면(로그인 안 된 상태) API 호출 안 함

    if (!userId) {
      setRooms([]);
      setLoading(false);
      setLoadError(null);
      return;
    }

    async function fetchRooms() {
      try {
        setLoading(true);
        setLoadError(null);

      console.log("GET /api/rooms params =", { userId });
        const res = await apiClient.get("/api/rooms", {
        params: { userId: userId },   // 키 이름 확실하게 지정
      });

        if (cancelled) return;

        const data = Array.isArray(res.data) ? res.data : [];
        setRooms(data);
      } catch (err) {
        if (cancelled) return;
        console.error("방 목록 불러오기 실패:", err);
        setLoadError("채팅방 목록을 불러오지 못했습니다.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    fetchRooms();
    return () => {
      cancelled = true;
    };
  }, [userId]);


  return (
    <div className="w-full min-h-[100svh] bg-gray-100">
      <div className="flex items-center justify-between p-4 border-b bg-white">
        <h1 className="text-xl font-bold">채팅 목록</h1>

        {/* 오른쪽: 사용자 정보 / 로그인 버튼 */}
        <div className="flex items-center gap-2">
          <button
            type="button"
            className="
              text-sm text-gray-600 
              border border-gray-300 
              rounded-lg px-3 py-1 
              hover:bg-gray-50 
              transition
            "
            onClick={onOpenUserInfo}
          >
            사용자 정보
          </button>

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
    </div>
  );
}

export default ChatList;


// test deploy