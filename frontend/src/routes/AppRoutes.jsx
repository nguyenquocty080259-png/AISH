import { Routes, Route, Navigate } from "react-router-dom";
import { ROUTES } from "../constants/routes";

import AppLayout from "./AppLayout";
import PrivateRoute from "./PrivateRoute";
import GuestRoute from "./GuestRoute";
import AdminRoute from "./AdminRoute";

import LoginPage from "../pages/auth/LoginPage";
import OAuthSuccessPage from "../pages/auth/OAuthSuccessPage";
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
import OnboardingPage from "../pages/onboarding/OnboardingPage";
import AiChatPage from "../pages/ai-chat/AiChatPage";
import MyReportsPage from "../pages/my-reports/MyReportsPage";
import AdminLayout from "../pages/admin/AdminLayout";
import AdminStatsPage from "../pages/admin/stats/AdminStatsPage";
import AdminAppealsPage from "../pages/admin/appeals/AdminAppealsPage";
import AdminReportsPage from "../pages/admin/reports/AdminReportsPage";
import AdminDocumentsPage from "../pages/admin/documents/AdminDocumentsPage";
import AdminSubjectsPage from "../pages/admin/subjects/AdminSubjectsPage";
import AdminKeywordsPage from "../pages/admin/keywords/AdminKeywordsPage";
import AdminUsersPage from "../pages/admin/users/AdminUsersPage";
import AdminSettingsPage from "../pages/admin/settings/AdminSettingsPage";
import NotFoundPage from "../pages/error/NotFoundPage";

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path={ROUTES.HOME} element={<HomePage />} />
        <Route path={ROUTES.AI_CHAT} element={<AiChatPage />} />
        <Route path={ROUTES.OAUTH_SUCCESS} element={<OAuthSuccessPage />} />

        <Route element={<GuestRoute />}>
          <Route path={ROUTES.LOGIN} element={<LoginPage />} />
          <Route path={ROUTES.SIGNUP} element={<SignUpPage />} />
          <Route path={ROUTES.VERIFY_OTP} element={<OtpPage />} />
          <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPasswordPage />} />
          <Route path={ROUTES.RESET_PASSWORD} element={<ResetPasswordPage />} />
        </Route>

        <Route element={<PrivateRoute />}>
          <Route path={ROUTES.ONBOARDING} element={<OnboardingPage />} />
          <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
          <Route path={ROUTES.DOCUMENTS} element={<DocumentPage />} />
          <Route path={ROUTES.TRASH} element={<TrashPage />} />
          <Route path={ROUTES.COMMUNITY} element={<CommunityPage />} />
          <Route path={ROUTES.SPACES} element={<CollectionsPage />} />
          <Route path={ROUTES.COLLECTION_DETAIL} element={<CollectionDetailPage />} />
          <Route path={ROUTES.DOCUMENT_DETAIL} element={<DocumentDetailPage />} />
          <Route path={ROUTES.FAVORITES} element={<FavoritesPage />} />
          <Route path={ROUTES.PROFILE} element={<ProfilePage />} />
          <Route path={ROUTES.MY_REPORTS} element={<MyReportsPage />} />
        </Route>

      </Route>

      <Route element={<AdminRoute />}>
        <Route path={ROUTES.ADMIN} element={<AdminLayout />}>
          <Route index element={<Navigate to={ROUTES.ADMIN_STATS} replace />} />
          <Route path="stats" element={<AdminStatsPage />} />
          <Route path="appeals" element={<AdminAppealsPage />} />
          <Route path="reports" element={<AdminReportsPage />} />
          <Route path="documents" element={<AdminDocumentsPage />} />
          <Route path="subjects" element={<AdminSubjectsPage />} />
          <Route path="keywords" element={<AdminKeywordsPage />} />
          <Route path="users" element={<AdminUsersPage />} />
          <Route path="settings" element={<AdminSettingsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
