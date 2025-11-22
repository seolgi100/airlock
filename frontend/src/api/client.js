// src/api/client.js
import axios from "axios";

const isLocal = window.location.hostname === "localhost";

// 로컬에서도, 서버에서도 동일하게 43.202.212.164:8080 API 서버
const API_BASE_URL = isLocal
  ? "http://43.202.212.164:8080"
  : "http://43.202.212.164:8080";

const apiClient = axios.create({
  baseURL: "/api",
  withCredentials: true,
});

export default apiClient;
