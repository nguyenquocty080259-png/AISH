import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { signup } from "../../services/authService";
import "./AuthPage.css";

export default function RegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError]     = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (form.password !== form.confirmPassword) {
      setError("Mật khẩu xác nhận không khớp");
      return;
    }

    setLoading(true);
    setError("");

    try {
      await signup({
        fullName: form.fullName,
        email: form.email,
        password: form.password,
      });

      // Chuyển sang OTP, truyền email qua state
      navigate("/otp-verification", { state: { email: form.email } });
    } catch (err) {
      setError(err.message || "Đăng ký thất bại");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-bg">
        <div className="auth-bg__blob auth-bg__blob--1" />
        <div className="auth-bg__blob auth-bg__blob--2" />
      </div>

      <div className="auth-card">
        <div className="auth-card__logo">
          <span className="auth-card__logo-icon">✦</span>
          <span className="auth-card__logo-text">AISH</span>
        </div>

        <h1 className="auth-card__title">Tạo tài khoản</h1>
        <p className="auth-card__subtitle">Bắt đầu hành trình học tập cùng AI</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-form__group">
            <label className="auth-form__label" htmlFor="fullName">Họ và tên</label>
            <input
              id="fullName"
              name="fullName"
              type="text"
              className="auth-form__input"
              placeholder="Nguyễn Văn A"
              value={form.fullName}
              onChange={handleChange}
              required
              autoComplete="name"
            />
          </div>

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
              autoComplete="new-password"
            />
          </div>

          <div className="auth-form__group">
            <label className="auth-form__label" htmlFor="confirmPassword">Xác nhận mật khẩu</label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              className="auth-form__input"
              placeholder="••••••••"
              value={form.confirmPassword}
              onChange={handleChange}
              required
              autoComplete="new-password"
            />
          </div>

          {error && <p className="auth-form__error">{error}</p>}

          <button
            type="submit"
            className="auth-form__btn"
            disabled={loading}
          >
            {loading ? "Đang đăng ký..." : "Đăng ký"}
          </button>
        </form>

        <p className="auth-card__footer">
          Đã có tài khoản?{" "}
          <Link to="/login" className="auth-card__link">Đăng nhập</Link>
        </p>
      </div>
    </div>
  );
}