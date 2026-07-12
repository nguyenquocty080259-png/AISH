export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  SIGNUP: "/signup",
  VERIFY_OTP: "/verify-otp",
  FORGOT_PASSWORD: "/forgot-password",
  RESET_PASSWORD: "/reset-password",
  DASHBOARD: "/dashboard",
  DOCUMENTS: "/documents",
  DOCUMENT_DETAIL: "/documents/:id",
  TRASH: "/trash",
  COMMUNITY: "/community",
  PROFILE: "/profile",
  AI_CHAT: "/ai-chat",
  // "Spaces" is the nav label for the Collections feature (backend: /api/collections).
  SPACES: "/collections",
  COLLECTION_DETAIL: "/collections/:id",
  FAVORITES: "/favorites",
  ADMIN: "/admin",
  ADMIN_STATS: "/admin/stats",
  ADMIN_APPEALS: "/admin/appeals",
  ADMIN_DOCUMENTS: "/admin/documents",
  ADMIN_SUBJECTS: "/admin/subjects",
  ADMIN_USERS: "/admin/users",
  NOT_FOUND: "/404",
};

export function buildRoute(route, params = {}) {
  return Object.entries(params).reduce(
    (path, [key, value]) => path.replace(`:${key}`, value),
    route
  );
}
