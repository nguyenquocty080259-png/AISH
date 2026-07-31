import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import logo from "../../assets/images/hivemind-logo.png";
import heroImg from "../../assets/images/hero-honeycomb.png";
import "./auth.css";

// Khung split dùng chung cho mọi trang auth (trừ OAuthSuccess). Panel trái = branding
// tĩnh (logo + hero + tagline thật), ẩn dưới 1024px. Panel phải = form (children).
export default function AuthShell({ children }) {
  const { t } = useTranslation();
  return (
    <div className="auth-shell">
      <aside className="auth-shell__aside">
        <Link to={ROUTES.HOME} className="auth-shell__brand has-custom-focus">
          <img src={logo} alt="" className="auth-shell__logo" />
          <span className="auth-shell__brand-name">HiveMind</span>
        </Link>

        <div className="auth-shell__hero">
          <img src={heroImg} alt="" className="auth-shell__hero-img" />
          <h2 className="auth-shell__hero-title">{t("auth.shell.heroTitle")}</h2>
          <p className="auth-shell__hero-text">{t("auth.shell.heroSubtitle")}</p>
        </div>

        <div className="auth-shell__tags">
          <span>{t("auth.shell.tagCollaborative")}</span>
          <span aria-hidden="true">·</span>
          <span>{t("auth.shell.tagNurturing")}</span>
          <span aria-hidden="true">·</span>
          <span>{t("auth.shell.tagOrganized")}</span>
        </div>
      </aside>

      <main className="auth-shell__main">
        <div className="auth-shell__panel">{children}</div>
      </main>
    </div>
  );
}
