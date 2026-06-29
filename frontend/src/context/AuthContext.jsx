import { createContext, useEffect, useState, useCallback } from "react";
import { STORAGE_KEYS } from "../constants/storageKeys";
import * as authApi from "../api/authApi";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(
    localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
  );
  // LƯU Ý: backend GET /api/auth/me hiện chưa trả "role" trong response,
  // nên role tạm thời luôn là null cho tới khi backend bổ sung field này.
  const [role, setRole] = useState(null);
  const [loading, setLoading] = useState(true);

  const clearSession = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    setAccessToken(null);
    setUser(null);
    setRole(null);
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
      .then((profile) => {
        setUser(profile);
        setRole(profile.role ?? null);
      })
      .catch(() => {
        clearSession();
      })
      .finally(() => setLoading(false));
  }, [clearSession]);

  // apiClient tự phát event này khi nhận 401 (token hết hạn / sai)
  useEffect(() => {
    const handleUnauthorized = () => clearSession();
    window.addEventListener("auth:unauthorized", handleUnauthorized);
    return () =>
      window.removeEventListener("auth:unauthorized", handleUnauthorized);
  }, [clearSession]);

  const login = useCallback(async (credentials) => {
    const data = await authApi.login(credentials);
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, data.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, data.refreshToken ?? "");
    setAccessToken(data.accessToken);

    const profile = await authApi.getMe();
    setUser(profile);
    setRole(profile.role ?? null);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(profile));
    return profile;
  }, []);

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

  const value = {
    user,
    role,
    accessToken,
    isAuthenticated: Boolean(accessToken),
    loading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
