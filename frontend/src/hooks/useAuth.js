import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

// Hook tiện lợi để lấy dữ liệu đăng nhập (user, role, token, login/logout...) từ AuthContext.
// Dùng thay vì gọi useContext(AuthContext) trực tiếp — báo lỗi rõ ràng nếu quên bọc <AuthProvider>.
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth phải được dùng bên trong <AuthProvider>");
  }
  return context;
}
