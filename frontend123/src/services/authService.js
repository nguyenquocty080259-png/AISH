import API_BASE_URL from "../api/api";

const AUTH_URL = `${API_BASE_URL}/api/auth`;

// ─── Signup ───────────────────────────────────────────────
// POST /api/auth/signup
// Body: { email, password, fullName }
// Response: "Register success"
export const signup = async ({ email, password, fullName }) => {
  const res = await fetch(`${AUTH_URL}/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password, fullName }),
  });

  const text = await res.text();
  if (!res.ok) throw new Error(text || "Signup failed");
  return text;
};

// ─── Login ────────────────────────────────────────────────
// POST /api/auth/login
// Body: { email, password }
// Response: { accessToken, refreshToken, tokenType }
export const login = async ({ email, password }) => {
  const res = await fetch(`${AUTH_URL}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || "Login failed");
  return data; // { accessToken, refreshToken, tokenType }
};


// ─── Verify OTP ───────────────────────────────────────────
// POST /api/auth/verify-otp
// Body: { email, otp }
// Response: "Email verified successfully"
export const verifyOtp = async ({ email, otp }) => {
  const res = await fetch(`${AUTH_URL}/verify-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, otp }),
  });

  const text = await res.text();
  if (!res.ok) throw new Error(text || "OTP verification failed");
  return text;
};

// ─── Resend OTP ───────────────────────────────────────────
// POST /api/auth/resend-otp
// Body: { email }
// Response: "OTP resent successfully"
export const resendOtp = async (email) => {
  const res = await fetch(`${AUTH_URL}/resend-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });

  const text = await res.text();
  if (!res.ok) throw new Error(text || "Resend OTP failed");
  return text;
};

// ======================================================
// Refresh Token
// POST /api/auth/refresh-token
// ======================================================

export const refreshAccessToken =
  async () => {
    const refreshToken =
      localStorage.getItem("refreshToken");

    const res = await fetch(
      `${AUTH_URL}/refresh-token`,
      {
        method: "POST",
        headers: {
          "Content-Type":
            "application/json",
        },
        body: JSON.stringify({
          refreshToken,
        }),
      }
    );

    const data = await res.json();

    if (!res.ok) {
      throw new Error(
        data.message ||
          "Refresh token failed"
      );
    }

    return data;
  };

// ─── Token helpers ────────────────────────────────────────
export const saveTokens = ({ accessToken, refreshToken }) => {
  localStorage.setItem("accessToken", accessToken);
  localStorage.setItem("refreshToken", refreshToken);
};

export const clearTokens = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
};

export const getAccessToken = () => localStorage.getItem("accessToken");

export const logout = async () => {
  const token = getAccessToken();

  const res = await fetch(`${AUTH_URL}/logout`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!res.ok) {
    throw new Error("Logout failed");
  }
};

