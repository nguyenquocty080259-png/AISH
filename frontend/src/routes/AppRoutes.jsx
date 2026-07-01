import { Routes, Route } from "react-router-dom";
import { ROUTES } from "../constants/routes";

import AppLayout from "./AppLayout";
import PrivateRoute from "./PrivateRoute";
import GuestRoute from "./GuestRoute";

import LoginPage from "../pages/auth/LoginPage";
import SignUpPage from "../pages/auth/SignUpPage";
import OtpPage from "../pages/auth/OtpPage";
import ForgotPasswordPage from "../pages/auth/ForgotPasswordPage";
import ResetPasswordPage from "../pages/auth/ResetPasswordPage";

import HomePage from "../pages/home/HomePage";
import DashboardPage from "../pages/dashboard/DashboardPage";
import DocumentPage from "../pages/document/DocumentPage";
import TrashPage from "../pages/document/TrashPage";
import CommunityPage from "../pages/community/CommunityPage";
import DocumentDetailPage from "../pages/document-detail/DocumentDetailPage";
import ProfilePage from "../pages/profile/ProfilePage";
import AiChatPage from "../pages/ai-chat/AiChatPage";
import NotFoundPage from "../pages/error/NotFoundPage";

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path={ROUTES.HOME} element={<HomePage />} />
        <Route path={ROUTES.AI_CHAT} element={<AiChatPage />} />

        <Route element={<GuestRoute />}>
          <Route path={ROUTES.LOGIN} element={<LoginPage />} />
          <Route path={ROUTES.SIGNUP} element={<SignUpPage />} />
          <Route path={ROUTES.VERIFY_OTP} element={<OtpPage />} />
          <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPasswordPage />} />
          <Route path={ROUTES.RESET_PASSWORD} element={<ResetPasswordPage />} />
        </Route>

        <Route element={<PrivateRoute />}>
          <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
          <Route path={ROUTES.DOCUMENTS} element={<DocumentPage />} />
          <Route path={ROUTES.TRASH} element={<TrashPage />} />
          <Route path={ROUTES.COMMUNITY} element={<CommunityPage />} />
          <Route path={ROUTES.DOCUMENT_DETAIL} element={<DocumentDetailPage />} />
          <Route path={ROUTES.PROFILE} element={<ProfilePage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}