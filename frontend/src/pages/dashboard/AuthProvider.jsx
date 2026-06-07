import { useState, useEffect } from "react";
import AuthContext from "./AuthContext";

export function AuthProvider({ children }) {

  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {

    async function fetchCurrentUser() {

      try {

        const token = localStorage.getItem("accessToken");

        const res = await fetch(
          "http://localhost:8080/api/auth/me",
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!res.ok) {
          throw new Error("Unauthenticated");
        }

        const data = await res.json();

        setUser(data);
        setRole(data.role);

      } catch (error) {
        console.error(error);
        setUser(null);
        setRole(null);

      } finally {

        setLoading(false);

      }
    }

    fetchCurrentUser();

  }, []);

  const logout = () => {

    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");

    setUser(null);
    setRole(null);

    window.location.href = "/login";
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        role,
        loading,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}