import { createContext, useEffect, useState, useCallback } from "react";
import { STORAGE_KEYS } from "../constants/storageKeys";
import * as authApi from "../api/authApi";
import * as profileApi from "../api/profileApi";
import { ROLES } from "../constants/roles";

export const AuthContext = createContext(null);

/**
 * NGUỒN SỰ THẬT DUY NHẤT về trạng thái đăng nhập trong toàn bộ app: ai đang đăng nhập, vai trò
 * gì, token là gì, hồ sơ đã điền đủ chưa. Bọc quanh toàn bộ ứng dụng (xem App.jsx) để mọi trang/
 * component đều đọc được qua hook {@link useAuth}.
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(
    localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
  );
  const [role, setRole] = useState(null);
  const [loading, setLoading] = useState(true);
  // Hồ sơ (dob, ...) được nạp một lần mỗi phiên đăng nhập, không nạp lại ở mỗi lần chuyển route -
  // PrivateRoute/AdminRoute chỉ đọc lại state này để quyết định có bắt onboarding hay không.
  const [profile, setProfile] = useState(null);

  // Xoá sạch phiên đăng nhập: bỏ token khỏi localStorage + reset toàn bộ state về rỗng.
  const clearSession = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    setAccessToken(null);
    setUser(null);
    setRole(null);
    setProfile(null);
  }, []);

  // Gọi API lấy lại hồ sơ mới nhất (dùng để kiểm tra có cần bắt onboarding không).
  const refreshProfile = useCallback(async () => {
    try {
      const myProfile = await profileApi.getMyProfile();
      setProfile(myProfile);
      return myProfile;
    } catch {
      // Không rõ dob thì đơn giản là chưa bắt được onboarding, không phải lỗi nghiêm trọng.
      return null;
    }
  }, []);

  // Khôi phục session khi load lại trang (nếu còn access token hợp lệ)
  useEffect(() => {
    const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    if (!token) {
      setLoading(false);
      return;
    }
    authApi
      .getMe()
      .then(async (me) => {
        setUser(me);
        setRole(me.role ?? null);
        await refreshProfile();
      })
      .catch(() => {
        clearSession();
      })
      .finally(() => setLoading(false));
  }, [clearSession, refreshProfile]);

  // apiClient tự phát event này khi nhận 401 (token hết hạn / sai)
  useEffect(() => {
    const handleUnauthorized = () => clearSession();
    window.addEventListener("auth:unauthorized", handleUnauthorized);
    return () =>
      window.removeEventListener("auth:unauthorized", handleUnauthorized);
  }, [clearSession]);

  // Áp dụng session cho một access token đã có sẵn (login thường HOẶC token nhận từ
  // redirect OAuth) - luôn đi qua GET /me + nạp profile một lần, không tách logic riêng.
  // Đầu vào: accessToken (+ refreshToken tuỳ chọn). Các bước: (1) lưu token vào localStorage;
  // (2) gọi GET /auth/me để lấy thông tin user; (3) nạp hồ sơ. Trả về: thông tin user vừa nạp.
  const applySession = useCallback(async (accessTokenValue, refreshTokenValue = "") => {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessTokenValue);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshTokenValue);
    setAccessToken(accessTokenValue);

    const me = await authApi.getMe();
    setUser(me);
    setRole(me.role ?? null);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(me));
    await refreshProfile();
    return me;
  }, [refreshProfile]);

  // Đăng nhập bằng email + mật khẩu: gọi API rồi áp phiên đăng nhập.
  const login = useCallback(async (credentials) => {
    const data = await authApi.login(credentials);
    return applySession(data.accessToken, data.refreshToken ?? "");
  }, [applySession]);

  // Redirect OAuth chỉ mang MỘT token (BE ký bằng generateToken, không có refresh token
  // riêng cho luồng social) - refreshToken để rỗng, phần còn lại giống hệt login thường.
  // Đăng nhập bằng token có sẵn — dùng khi redirect về từ OAuth (Google/GitHub).
  const loginWithToken = useCallback(async (accessTokenValue) => {
    return applySession(accessTokenValue, "");
  }, [applySession]);

  // Đăng xuất: gọi API rồi luôn xoá phiên phía client (kể cả khi API lỗi).
  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // Backend chưa có cơ chế revoke token, logout phía server chỉ mang tính ghi log.
      // Dù request lỗi vẫn phải xoá session phía client.
    } finally {
      clearSession();
    }
  }, [clearSession]);

  // Có cần chuyển hướng người dùng tới trang onboarding không: đã đăng nhập, không phải Admin,
  // đã nạp hồ sơ, nhưng còn thiếu ngày sinh hoặc họ tên.
  const needsOnboarding = Boolean(
    accessToken &&
      role &&
      role !== ROLES.ADMIN &&
      profile &&
      (!profile.dob || !profile.fullName?.trim())
  );

  const value = {
    user,
    role,
    accessToken,
    isAuthenticated: Boolean(accessToken),
    loading,
    login,
    loginWithToken,
    logout,
    profile,
    refreshProfile,
    needsOnboarding,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
