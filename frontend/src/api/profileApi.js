import apiClient from "../lib/apiClient";

export function getMyProfile() {
  return apiClient.get("/profile/me").then((res) => res.data);
}

export function updateMyProfile(data) {
  return apiClient.put("/profile/me", data).then((res) => res.data);
}

export function completeOnboarding(data) {
  return apiClient.put("/profile/onboarding", data).then((res) => res.data);
}
