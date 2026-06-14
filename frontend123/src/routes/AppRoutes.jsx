import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

import HomePage             from "../pages/home/HomePage";
import LoginPage            from "../pages/auth/LoginPage";
import RegisterPage         from "../pages/auth/RegisterPage";
import OTPVerificationPage  from "../pages/auth/OTPVerificationPage";
import DashboardLayout from "../pages/dashboard/DashboardLayout";
import StudentDashboard from "../pages/dashboard/StudentDashboard";
import AdminDashboard from "../pages/dashboard/AdminDashboard";
import NotFoundPage         from "../pages/error/Notfoundpage";
import Profile from "../pages/profile/Profile";

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

function DashboardRouter() {
  const { role } = useAuth();

  return (
    <DashboardLayout>
      {role === "admin"
        ? <AdminDashboard />
        : <StudentDashboard />}
    </DashboardLayout>
  );
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
            <DashboardRouter />
          </PrivateRoute>
        }
      />

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
      <Route
          path="/profile"
          element={
            <PrivateRoute>
              <DashboardLayout>
                <Profile />
              </DashboardLayout>
            </PrivateRoute>
          }
        />
    </Routes>
    
  );
}