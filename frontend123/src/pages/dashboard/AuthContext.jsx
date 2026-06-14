import { createContext, useContext, useState, useEffect } from "react";

// ============================================================
// AuthContext — kết nối backend để lấy user & role
// ============================================================
// Cách dùng:
//   const { user, role, loading, logout } = useAuth();
//
// role trả về: "student" | "admin" | null
// ============================================================

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [role, setRole]       = useState(null);  // "student" | "admin"
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // TODO: thay bằng API call thực tế
    // Ví dụ: GET /api/auth/me — trả về { id, name, school, role, ... }
    async function fetchCurrentUser() {
      try {
        const res = await fetch("/api/auth/me", {
          credentials: "include", // gửi cookie session
        });

        if (!res.ok) throw new Error("Unauthenticated");

        const data = await res.json();

        setUser(data);
        setRole(data.role); // backend trả về field "role": "student" | "admin"
      } catch {
        setUser(null);
        setRole(null);
      } finally {
        setLoading(false);
      }
    }

    fetchCurrentUser();
  }, []);

  async function logout() {
    // TODO: thay bằng API call thực tế
    await fetch("/api/auth/logout", { method: "POST", credentials: "include" });
    setUser(null);
    setRole(null);
    window.location.href = "/login";
  }

  return (
    <AuthContext.Provider value={{ user, role, loading, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth phải dùng trong AuthProvider");
  return ctx;
}
