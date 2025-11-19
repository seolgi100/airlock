// src/api.js
import axios from "axios";

// 개발 환경(dev)에서는 baseURL 안 쓰고, 상대경로만 사용
// → http://localhost:3000/rooms 로 요청 → proxy가 8080으로 포워딩
const api = axios.create({
  baseURL:
    process.env.NODE_ENV === "production"
      ? process.env.REACT_APP_API_URL // 배포용에서만 사용
      : "",                            // dev에서는 빈 문자열
});

export default api;
