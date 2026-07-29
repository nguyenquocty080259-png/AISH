import apiClient from "../lib/apiClient";

export function getMyAppeals() {
  return apiClient.get("/appeals/mine").then((res) => res.data);
}
