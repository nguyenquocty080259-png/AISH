import apiClient from "../lib/apiClient";

export function getInteractionSummary() {
  return apiClient.get("/interactions/summary").then((res) => res.data);
}
