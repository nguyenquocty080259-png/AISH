import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";
import { ROLES } from "../constants/roles";

// Bọc các route chỉ dành cho khách (chưa đăng nhập), ví dụ Login, SignUp.
// Nếu đã đăng nhập rồi mà cố vào /login thì đẩy về Dashboard.
export default function GuestRoute() {
  const { isAuthenticated, role, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (isAuthenticated) {
    return (
      <Navigate
        to={role === ROLES.ADMIN ? ROUTES.ADMIN : ROUTES.DASHBOARD}
        replace
      />
    );
  }

  return <Outlet />;
}
