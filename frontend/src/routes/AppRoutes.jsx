import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

import HomePage             from "../pages/home/HomePage";
import LoginPage            from "../pages/auth/LoginPage";
import RegisterPage         from "../pages/auth/RegisterPage";
import OTPVerificationPage  from "../pages/auth/OTPVerificationPage";
import DashboardPage        from "../pages/dashboard/DashboardPage";
import NotFoundPage         from "../pages/error/Notfoundpage";

// ── Bảo vệ route cần đăng nhập ──────────────────────────
function PrivateRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) return <div className="auth-loading">Đang tải...</div>;

  return user ? children : <Navigate to="/login" replace />;
}

// ── Redirect nếu đã đăng nhập ────────────────────────────
function GuestRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) return null;

  return user ? <Navigate to="/dashboard" replace /> : children;
}

// ── Routes ───────────────────────────────────────────────
export default function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/" element={<HomePage />} />

      {/* Guest only — đã login thì redirect dashboard */}
      <Route path="/login"            element={<GuestRoute><LoginPage /></GuestRoute>} />
      <Route path="/register"         element={<GuestRoute><RegisterPage /></GuestRoute>} />
      <Route path="/otp-verification" element={<OTPVerificationPage />} />

      {/* Private — cần login */}
      <Route
        path="/dashboard"
        element={
          <PrivateRoute>
            <DashboardPage />
          </PrivateRoute>
        }
      />

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}