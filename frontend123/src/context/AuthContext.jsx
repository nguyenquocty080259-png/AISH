import { createContext, useContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import API_BASE_URL from "../api/api";
import {
  getAccessToken,
  clearTokens,
  logout as logoutApi
} from "../services/authService";
const AuthContext = createContext(null);


export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true); // chỉ true lần đầu
  const navigate = useNavigate();
  // Chỉ chạy 1 lần khi app khởi động — kiểm tra token cũ
  useEffect(() => {
    async function fetchMe() {
      const token = getAccessToken();

      if (!token) {
        setLoading(false);
        return;
      }

      try {
        const res = await fetch(`${API_BASE_URL}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!res.ok) throw new Error("Unauthorized");

        const data = await res.json();
        setUser(data);
      } catch {
        clearTokens();
        setUser(null);
      } finally {
        setLoading(false);
      }
    }

    fetchMe();
  }, []); // ← dependency rỗng, chỉ chạy 1 lần

const logout = async () => {
  console.log("1. logout start");

  try {
    await logoutApi();
    console.log("2. api success");
  } catch (e) {
    console.error("logout error", e);
  } finally {
    console.log("3. clear token");

    clearTokens();
    setUser(null);

    console.log("4. navigate home");

    navigate("/");
  }
};

  return (
    <AuthContext.Provider value={{user, role: user?.role, setUser, loading, logout}}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}

export default AuthContext;