import { Routes, Route } from "react-router-dom";
import { ROUTES } from "../constants/routes";
import AppLayout from "./AppLayout";
import PrivateRoute from "./PrivateRoute";
import GuestRoute from "./GuestRoute";

import LoginPage from "../pages/auth/LoginPage";
import SignUpPage from "../pages/auth/SignUpPage";
import OtpPage from "../pages/auth/OtpPage";
import HomePage from "../pages/home/HomePage";
import DashboardPage from "../pages/dashboard/DashboardPage";
import DocumentDetailPage from "../pages/document-detail/DocumentDetailPage";
import ProfilePage from "../pages/profile/ProfilePage";
import AiChatPage from "../pages/ai-chat/AiChatPage";
import NotFoundPage from "../pages/error/NotFoundPage";
import DocumentPage from "../pages/document/DocumentPage";
import TrashPage from "../pages/document/TrashPage";

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        {/* Public */}
        <Route path={ROUTES.HOME} element={<HomePage />} />
        <Route path={ROUTES.AI_CHAT} element={<AiChatPage />} />

        {/* Chỉ dành cho khách (chưa đăng nhập) */}
        <Route element={<GuestRoute />}>
          <Route path={ROUTES.LOGIN} element={<LoginPage />} />
          <Route path={ROUTES.SIGNUP} element={<SignUpPage />} />
          <Route path={ROUTES.VERIFY_OTP} element={<OtpPage />} />
        </Route>

        {/* Chỉ dành cho user đã đăng nhập */}
        <Route element={<PrivateRoute />}>
          <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
          <Route path={ROUTES.DOCUMENTS} element={<DocumentPage />} />
          <Route path={ROUTES.TRASH} element={<TrashPage />} />
          <Route path={ROUTES.DOCUMENT_DETAIL} element={<DocumentDetailPage />} />
          <Route path={ROUTES.PROFILE} element={<ProfilePage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
