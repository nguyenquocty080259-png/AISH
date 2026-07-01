// Mirror đúng enum Role bên backend (com.aish.mvc.entity.enums.Role).
// LƯU Ý: GET /api/auth/me hiện tại CHƯA trả field "role" trong response.
// Khi backend bổ sung, AuthContext sẽ tự nhận và PrivateRoute có thể check role
// dựa theo các giá trị khai báo ở đây — không hardcode "ADMIN" / "USER" trong code.
export const ROLES = {
  ADMIN: "ADMIN",
  USER: "USER",
};
