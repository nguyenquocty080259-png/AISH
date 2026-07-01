import { Link } from "react-router-dom";
import { useSignUpPage } from "./hooks/useSignUpPage";
import { ROUTES } from "../../constants/routes";
import "./auth.css";

export default function SignUpPage() {
  const {
    fullName,
    setFullName,
    email,
    setEmail,
    password,
    setPassword,
    submitting,
    handleSubmit,
  } = useSignUpPage();

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="auth-card__brand">AISH</p>
        <h1 className="auth-card__title">Tạo tài khoản</h1>
        <p className="auth-card__subtitle">
          Tham gia AISH để lưu trữ, chia sẻ và trò chuyện với AI về tài liệu học tập.
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-field">
            <label htmlFor="fullName">Họ và tên</label>
            <input
              id="fullName"
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Nguyễn Văn A"
            />
          </div>

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
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Tối thiểu 6 ký tự"
            />
          </div>

          <button className="auth-submit" type="submit" disabled={submitting}>
            {submitting ? "Đang đăng ký..." : "Đăng ký"}
          </button>
        </form>

        <p className="auth-footer">
          Đã có tài khoản? <Link to={ROUTES.LOGIN}>Đăng nhập</Link>
        </p>
      </div>
    </div>
  );
}
