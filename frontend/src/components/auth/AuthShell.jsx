import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import logo from "../../assets/images/hivemind-logo.png";
import heroImg from "../../assets/images/hero-honeycomb.png";

// Khung split dùng chung cho mọi trang auth (trừ OAuthSuccess). Panel trái = branding
// tĩnh (logo + hero + tagline thật), ẩn dưới lg. Panel phải = form (children).
export default function AuthShell({ children }) {
  const { t } = useTranslation();
  return (
    <div className="min-h-screen flex text-app">
      <aside className="hidden lg:flex lg:w-1/2 bg-surface-soft flex-col justify-between p-12">
        <Link to={ROUTES.HOME} className="flex items-center gap-3">
          <img src={logo} alt="HiveMind" className="w-9 h-9 object-contain" />
          <span className="text-lg font-bold text-primary tracking-tight">HiveMind</span>
        </Link>

        <div className="w-full max-w-md mx-auto">
          <div className="relative">
            <div className="absolute inset-0 bg-surface rounded-card scale-105 -z-10" />
            <img src={heroImg} alt="HiveMind" className="w-full rounded-card border border-border shadow-lg" />
          </div>
          <h2 className="mt-10 text-3xl font-bold leading-tight tracking-tight">
            {t("auth.shell.heroTitle")}
          </h2>
          <p className="mt-4 text-secondary text-base">
            {t("auth.shell.heroSubtitle")}
          </p>
        </div>

        <div className="flex items-center gap-4 text-xs font-semibold uppercase tracking-widest text-secondary">
          <span>{t("auth.shell.tagCollaborative")}</span><span>·</span><span>{t("auth.shell.tagNurturing")}</span><span>·</span><span>{t("auth.shell.tagOrganized")}</span>
        </div>
      </aside>

      <main className="flex-1 flex items-center justify-center bg-surface px-6 py-12">
        <div className="w-full max-w-[420px]">{children}</div>
      </main>
    </div>
  );
}
