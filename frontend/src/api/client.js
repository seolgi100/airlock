// src/api/client.js
import axios from "axios";

const API_BASE_URL =
  window.location.hostname === "localhost"
    ? ""                           
    : `https://${window.location.hostname}`;

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

export default apiClient;