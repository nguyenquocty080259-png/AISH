import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/profile/* tại đây.

// Gọi API GET /profile/me — lấy hồ sơ của tôi.
export function getMyProfile() {
  return apiClient.get("/profile/me").then((res) => res.data);
}

// Gọi API PUT /profile/me — cập nhật hồ sơ.
export function updateMyProfile(data) {
  return apiClient.put("/profile/me", data).then((res) => res.data);
}

// Gọi API PUT /profile/onboarding — hoàn tất thiết lập hồ sơ lần đầu.
export function completeOnboarding(data) {
  return apiClient.put("/profile/onboarding", data).then((res) => res.data);
}

// Gọi API POST /profile/avatar — tải ảnh đại diện mới lên (multipart/form-data).
export function uploadAvatar(file) {
  const formData = new FormData();

  formData.append("file", file);

  return apiClient.post(
    "/profile/avatar",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
  ).then((res) => res.data);
}