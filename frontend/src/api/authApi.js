import apiClient from "../lib/apiClient";

// Tất cả request đi qua /api/auth/* tập trung tại đây.
// Hook (useAuth, useLoginPage...) gọi các hàm này, KHÔNG gọi apiClient trực tiếp.

// Gọi API POST /auth/signup — đăng ký tài khoản, gửi OTP xác minh về email.
export function signup({ email, password, fullName }) {
  return apiClient.post("/auth/signup", { email, password, fullName });
}

// Gọi API POST /auth/login — đăng nhập bằng email + mật khẩu.
export function login({ email, password }) {
  // Trả về { accessToken, refreshToken, tokenType }
  return apiClient.post("/auth/login", { email, password }).then((res) => res.data);
}

// Gọi API POST /auth/verify-otp — xác minh OTP để kích hoạt tài khoản.
export function verifyOtp({ email, otp }) {
  return apiClient.post("/auth/verify-otp", { email, otp });
}

// Gọi API POST /auth/resend-otp — gửi lại OTP mới.
export function resendOtp({ email }) {
  return apiClient.post("/auth/resend-otp", { email });
}

// Gọi API GET /auth/me — thông tin cơ bản của user đang đăng nhập.
export function getMe() {
  // Trả về { email, fullName, status } - backend hiện chưa trả "role"
  return apiClient.get("/auth/me").then((res) => res.data);
}

// Gọi API POST /auth/logout — đăng xuất.
export function logout() {
  return apiClient.post("/auth/logout");
}

// Gọi API POST /auth/forgot-password — bắt đầu luồng quên mật khẩu, gửi OTP về email.
export function forgotPassword({ email }) {
  return apiClient.post(
    "/auth/forgot-password",
    { email }
  );
}

// Gọi API POST /auth/verify-forgot-password — xác minh OTP quên mật khẩu, đổi lấy resetToken.
export function verifyForgotPassword(
  { email, otp }
) {
  return apiClient.post(
    "/auth/verify-forgot-password",
    { email, otp }
  );
}

// Gọi API POST /auth/reset-password — đặt mật khẩu mới bằng resetToken.
export function resetPassword(
  { resetToken, password }
) {
  return apiClient.post(
    "/auth/reset-password",
    {
      resetToken,
      password
    }
  );
}

