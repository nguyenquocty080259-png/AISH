// Toàn bộ path của app khai báo tại đây.
// Component không được tự viết "/login", "/dashboard"... trực tiếp.
export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  SIGNUP: "/signup",
  VERIFY_OTP: "/verify-otp",
  DASHBOARD: "/dashboard",
  DOCUMENTS: "/documents",
  DOCUMENT_DETAIL: "/documents/:id",
  PROFILE: "/profile",
  AI_CHAT: "/ai-chat",
  NOT_FOUND: "/404",
};

// Helper build path động, ví dụ: buildRoute(ROUTES.DOCUMENT_DETAIL, { id: 12 })
export function buildRoute(route, params = {}) {
  return Object.entries(params).reduce(
    (path, [key, value]) => path.replace(`:${key}`, value),
    route
  );
}
