// src/api/client.js
import axios from "axios";

const API_BASE_URL =
  window.location.hostname === "localhost"
    ? "http://localhost:8080"
    : "http://43.202.212.164:8080"; 

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default apiClient;
