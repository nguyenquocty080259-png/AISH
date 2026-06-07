import { Routes, Route, Navigate } from "react-router-dom";

import { useAuth } from "../pages/dashboard/AuthContext";

import HomePage       from "../pages/home/HomePage";
import LoginPage      from "../pages/auth/LoginPage";
import RegisterPage   from "../pages/auth/RegisterPage";
import NotFoundPage   from "../pages/error/NotFoundPage";
import Dashboard      from "../pages/dashboard/Dashboard";

// ============================================================
// PrivateRoute — bảo vệ các route cần đăng nhập
// Nếu chưa login → redirect về /login
// Nếu đang load (chờ API /auth/me) → hiện loading nhỏ
// ============================================================

function PrivateRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) return null; // hoặc thay bằng <LoadingSpinner />

  return user ? children : <Navigate to="/login" replace />;
}

// ============================================================
// AppRoutes
// ============================================================

function AppRoutes() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/"          element={<HomePage />}     />
      <Route path="/login"     element={<LoginPage />}    />
      <Route path="/register"  element={<RegisterPage />} />

      {/* Protected routes — cần đăng nhập */}
      <Route
        path="/dashboard"
        element={
          <PrivateRoute>
            <Dashboard />
          </PrivateRoute>
        }
      />

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default AppRoutes;