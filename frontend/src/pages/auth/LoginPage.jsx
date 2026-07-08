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
        <p className="auth-card__brand">HiveMind</p>
        <h1 className="auth-card__title">Đăng nhập</h1>
        <p className="auth-card__subtitle">
          Tiếp tục học tập và chia sẻ tài liệu cùng HiveMind.
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

          <div className="auth-actions">
            <p className="forgot-password-link">
              <Link to={ROUTES.FORGOT_PASSWORD}> 
              Quên mật khẩu? </Link>
            </p>
          </div>

          <button className="auth-submit" type="submit" disabled={submitting}>
            {submitting ? "Đang đăng nhập..." : "Đăng nhập"}
          </button>


        </form>
        <button
          type="button"
          className="social-btn google"
          onClick={() => {
            window.location.href =
              "http://localhost:8080/oauth2/authorization/google";
          }}
        >
          Google
        </button>

        <button
        type="button"
        className="social-btn github"
        onClick={() => {
          window.location.href =
            "http://localhost:8080/oauth2/authorization/github";
        }}
      >
        GitHub
      </button>

      <button
        type="button"
        className="social-btn facebook"
        onClick={() => {
          window.location.href =
            "http://localhost:8080/oauth2/authorization/facebook";
        }}
      >
        Facebook
      </button>
        <p className="auth-footer">
          Chưa có tài khoản? <Link to={ROUTES.SIGNUP}>Đăng ký ngay</Link>
        </p>
      </div>
    </div>
  );
}
