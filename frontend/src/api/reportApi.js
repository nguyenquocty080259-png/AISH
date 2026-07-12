import apiClient from "../lib/apiClient";

export function createReport(payload) {
  return apiClient.post("/reports", payload).then((res) => res.data);
}

export function getMyReports() {
  return apiClient.get("/reports/mine").then((res) => res.data);
}
