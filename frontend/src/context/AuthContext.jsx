import { createContext, useEffect, useState, useCallback } from "react";
import { STORAGE_KEYS } from "../constants/storageKeys";
import * as authApi from "../api/authApi";
import * as profileApi from "../api/profileApi";
import { ROLES } from "../constants/roles";

export const AuthContext = createContext(null);

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

  const clearSession = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    setAccessToken(null);
    setUser(null);
    setRole(null);
    setProfile(null);
  }, []);

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

  const login = useCallback(async (credentials) => {
    const data = await authApi.login(credentials);
    return applySession(data.accessToken, data.refreshToken ?? "");
  }, [applySession]);

  // Redirect OAuth chỉ mang MỘT token (BE ký bằng generateToken, không có refresh token
  // riêng cho luồng social) - refreshToken để rỗng, phần còn lại giống hệt login thường.
  const loginWithToken = useCallback(async (accessTokenValue) => {
    return applySession(accessTokenValue, "");
  }, [applySession]);

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

  const needsOnboarding = Boolean(
    accessToken && role && role !== ROLES.ADMIN && profile && !profile.dob
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
