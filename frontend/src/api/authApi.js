import apiClient from "../lib/apiClient";

// Tất cả request đi qua /api/auth/* tập trung tại đây.
// Hook (useAuth, useLoginPage...) gọi các hàm này, KHÔNG gọi apiClient trực tiếp.

export function signup({ email, password, fullName }) {
  return apiClient.post("/auth/signup", { email, password, fullName });
}

export function login({ email, password }) {
  // Trả về { accessToken, refreshToken, tokenType }
  return apiClient.post("/auth/login", { email, password }).then((res) => res.data);
}

export function verifyOtp({ email, otp }) {
  return apiClient.post("/auth/verify-otp", { email, otp });
}

export function resendOtp({ email }) {
  return apiClient.post("/auth/resend-otp", { email });
}

export function getMe() {
  // Trả về { email, fullName, status } - backend hiện chưa trả "role"
  return apiClient.get("/auth/me").then((res) => res.data);
}

export function logout() {
  return apiClient.post("/auth/logout");
}

export function forgotPassword(email) {
  return apiClient.post(
    "/auth/forgot-password",
    { email }
  );
}

export function verifyForgotPassword(
  { email, otp }
) {
  return apiClient.post(
    "/auth/verify-forgot-password",
    { email, otp }
  );
}

export function resetPassword(
  { email, password }
) {
  return apiClient.post(
    "/auth/reset-password",
    {
      email,
      password
    }
  );
}

