import { Link } from "react-router-dom";
import { useLoginPage } from "./hooks/useLoginPage";
import { ROUTES } from "../../constants/routes";
import "./auth.css";

export default function LoginPage() {
  const { email, setEmail, password, setPassword, submitting, handleSubmit } =
    useLoginPage();

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="auth-card__brand">AISH</p>
        <h1 className="auth-card__title">Đăng nhập</h1>
        <p className="auth-card__subtitle">
          Tiếp tục học tập và chia sẻ tài liệu cùng AISH.
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ban@email.com"
            />
          </div>

          <div className="auth-field">
            <label htmlFor="password">Mật khẩu</label>
            <input
              id="password"
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>

          <button className="auth-submit" type="submit" disabled={submitting}>
            {submitting ? "Đang đăng nhập..." : "Đăng nhập"}
          </button>
        </form>

        <p className="auth-footer">
          Chưa có tài khoản? <Link to={ROUTES.SIGNUP}>Đăng ký ngay</Link>
        </p>
      </div>
    </div>
  );
}
