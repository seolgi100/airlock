// src/api/client.js
import axios from "axios";

const isLocal = window.location.hostname === "localhost";

// 로컬에서도, 서버에서도 동일하게 15.165.2.31:8080 API 서버
const API_BASE_URL = isLocal
  ? "http://15.165.2.31:8080"
  : "http://15.165.2.31:8080";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

export default apiClient;
