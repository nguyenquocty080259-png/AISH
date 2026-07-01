import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";
import { ROLES } from "../constants/roles";

// Cùng cấu trúc với PrivateRoute, chỉ thêm điều kiện role === ADMIN.
// Đã đăng nhập nhưng không phải admin -> đẩy về Dashboard (không có trang 403 riêng).
export default function AdminRoute() {
  const { isAuthenticated, role, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  if (role !== ROLES.ADMIN) {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return <Outlet />;
}
