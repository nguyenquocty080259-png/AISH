import { useOAuthSuccessPage } from "./hooks/useOAuthSuccessPage";
import "./auth.css";

export default function OAuthSuccessPage() {
  useOAuthSuccessPage();

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="auth-card__brand">HiveMind</p>
        <h1 className="auth-card__title">Đang đăng nhập...</h1>
        <p className="auth-card__subtitle">Vui lòng chờ trong giây lát.</p>
      </div>
    </div>
  );
}
