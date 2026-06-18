import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";

// Bọc các route chỉ dành cho khách (chưa đăng nhập), ví dụ Login, SignUp.
// Nếu đã đăng nhập rồi mà cố vào /login thì đẩy về Dashboard.
export default function GuestRoute() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (isAuthenticated) {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return <Outlet />;
}
