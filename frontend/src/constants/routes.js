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
  NOT_FOUND: "/404",
};

export function buildRoute(route, params = {}) {
  return Object.entries(params).reduce(
    (path, [key, value]) => path.replace(`:${key}`, value),
    route
  );
}