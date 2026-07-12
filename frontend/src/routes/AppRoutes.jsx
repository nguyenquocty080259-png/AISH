import { Routes, Route, Navigate } from "react-router-dom";
import { ROUTES } from "../constants/routes";

import AppLayout from "./AppLayout";
import PrivateRoute from "./PrivateRoute";
import GuestRoute from "./GuestRoute";
import AdminRoute from "./AdminRoute";

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
import CollectionsPage from "../pages/collections/CollectionsPage";
import CollectionDetailPage from "../pages/collection-detail/CollectionDetailPage";
import DocumentDetailPage from "../pages/document-detail/DocumentDetailPage";
import FavoritesPage from "../pages/favorites/FavoritesPage";
import ProfilePage from "../pages/profile/ProfilePage";
import AiChatPage from "../pages/ai-chat/AiChatPage";
import AdminLayout from "../pages/admin/AdminLayout";
import AdminStatsPage from "../pages/admin/stats/AdminStatsPage";
import AdminAppealsPage from "../pages/admin/appeals/AdminAppealsPage";
import AdminDocumentsPage from "../pages/admin/documents/AdminDocumentsPage";
import AdminSubjectsPage from "../pages/admin/subjects/AdminSubjectsPage";
import NotFoundPage from "../pages/error/NotFoundPage";
import UiPreviewPage from "../pages/ui-preview/UiPreviewPage";

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
          <Route path={ROUTES.SPACES} element={<CollectionsPage />} />
          <Route path={ROUTES.COLLECTION_DETAIL} element={<CollectionDetailPage />} />
          <Route path={ROUTES.DOCUMENT_DETAIL} element={<DocumentDetailPage />} />
          <Route path={ROUTES.FAVORITES} element={<FavoritesPage />} />
          <Route path={ROUTES.PROFILE} element={<ProfilePage />} />
          {/* TEMPORARY — internal ui/ primitives preview, not linked in nav. Remove before shipping. */}
          <Route path="/_ui-preview" element={<UiPreviewPage />} />
        </Route>

      </Route>

      <Route element={<AdminRoute />}>
        <Route path={ROUTES.ADMIN} element={<AdminLayout />}>
          <Route index element={<Navigate to={ROUTES.ADMIN_STATS} replace />} />
          <Route path="stats" element={<AdminStatsPage />} />
          <Route path="appeals" element={<AdminAppealsPage />} />
          <Route path="documents" element={<AdminDocumentsPage />} />
          <Route path="subjects" element={<AdminSubjectsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
