import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";

// Bọc các route chỉ cho phép truy cập khi đã đăng nhập.
// Dùng trong AppRoutes: <Route element={<PrivateRoute />}>...</Route>
export default function PrivateRoute() {
  const { isAuthenticated, loading, needsOnboarding } = useAuth();
  const location = useLocation();

  if (loading) {
    return null; // có thể thay bằng spinner chung khi cần
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  // Bắt hoàn tất onboarding (dob) trước khi vào bất kỳ trang riêng tư nào, trừ chính /onboarding
  // (tránh redirect loop). ADMIN không phải qua bước này (needsOnboarding tự false với role ADMIN).
  if (needsOnboarding && location.pathname !== ROUTES.ONBOARDING) {
    return <Navigate to={ROUTES.ONBOARDING} replace state={{ from: location }} />;
  }

  return <Outlet />;
}
