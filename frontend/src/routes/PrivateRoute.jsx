import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";

// Bọc các route chỉ cho phép truy cập khi đã đăng nhập.
// Dùng trong AppRoutes: <Route element={<PrivateRoute />}>...</Route>
export default function PrivateRoute() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return null; // có thể thay bằng spinner chung khi cần
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  return <Outlet />;
}
