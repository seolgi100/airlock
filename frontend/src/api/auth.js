import apiClient from "./client";

export async function validateToken() {
  const res = await apiClient.get("/auth/validate");
  return res.data; // { id, username, displayName }
}