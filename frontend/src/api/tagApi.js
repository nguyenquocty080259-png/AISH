import apiClient from "../lib/apiClient";

export function getAll() {
  return apiClient.get("/tags").then((res) => res.data);
}
