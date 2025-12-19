// ChatRoomCreate.jsx
import { useState } from "react";
import apiClient from "../../api/client";

export default function ChatRoomCreate() {
  const [open, setOpen] = useState(false);
  const [username, setUsername] = useState("");

  const createRoom = async () => {
    const u = username.trim();
    if (!u) return;
    await apiClient.post("/rooms", { targetUsername: u });
    setUsername("");
    setOpen(false);
  };

  return (
    <>
      {open && (
        <div className="fixed right-6 bottom-[90px] z-[9999] flex gap-2 rounded-xl border border-gray-200 bg-white p-3 shadow-xl">
          <input
            className="w-44 rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="상대 username"
            onKeyDown={(e) => e.key === "Enter" && createRoom()}
          />
          <button
            className="rounded-lg bg-black px-3 py-2 text-sm text-white"
            onClick={createRoom}
          >
            생성
          </button>
        </div>
      )}

      <button
        className="fixed right-6 bottom-6 z-[9999] h-14 w-14 rounded-full bg-black text-2xl text-white"
        onClick={() => setOpen((v) => !v)}
        aria-label="채팅방 생성"
        title="채팅방 만들기"
      >
        +
      </button>
    </>
  );
}
