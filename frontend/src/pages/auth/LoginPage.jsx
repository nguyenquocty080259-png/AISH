
// LoginPage.jsx
import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { login, saveTokens } from "../../services/authService";
import { useAuth } from "../../context/AuthContext"; // ← thêm
import "./AuthPage.css";

export default function LoginPage() {
  const navigate = useNavigate();
  const { setUser } = useAuth(); // ← thêm

  const [form, setForm]       = useState({ email: "", password: "" });
  const [error, setError]     = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
  };

const handleSubmit = async (e) => {
  e.preventDefault();
  setLoading(true);
  setError("");

  try {
    const data = await login(form);
    saveTokens(data);
    setUser({ email: form.email }); // set user trước
    navigate("/dashboard", { replace: true }); // replace: true để không back về login được
  } catch (err) {
    setError(err.message || "Đăng nhập thất bại");
  } finally {
    setLoading(false);
  }
};

  // ... phần JSX giữ nguyên



  return (
    <div className="auth-page">
      {/* Decorative background */}
      <div className="auth-bg">
        <div className="auth-bg__blob auth-bg__blob--1" />
        <div className="auth-bg__blob auth-bg__blob--2" />
      </div>

      <div className="auth-card">
        {/* Logo */}
        <div className="auth-card__logo">
          <span className="auth-card__logo-icon">✦</span>
          <span className="auth-card__logo-text">AISH</span>
        </div>

        <h1 className="auth-card__title">Đăng nhập</h1>
        <p className="auth-card__subtitle">Chào mừng bạn trở lại</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-form__group">
            <label className="auth-form__label" htmlFor="email">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              className="auth-form__input"
              placeholder="example@email.com"
              value={form.email}
              onChange={handleChange}
              required
              autoComplete="email"
            />
          </div>

          <div className="auth-form__group">
            <label className="auth-form__label" htmlFor="password">Mật khẩu</label>
            <input
              id="password"
              name="password"
              type="password"
              className="auth-form__input"
              placeholder="••••••••"
              value={form.password}
              onChange={handleChange}
              required
              autoComplete="current-password"
            />
          </div>

          {error && <p className="auth-form__error">{error}</p>}

          <button
            type="submit"
            className="auth-form__btn"
            disabled={loading}
          >
            {loading ? "Đang đăng nhập..." : "Đăng nhập"}
          </button>
        </form>

        <p className="auth-card__footer">
          Chưa có tài khoản?{" "}
          <Link to="/register" className="auth-card__link">Đăng ký ngay</Link>
        </p>
      </div>
    </div>
  );
}