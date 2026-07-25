import { useTranslation } from "react-i18next";
import { useOAuthSuccessPage } from "./hooks/useOAuthSuccessPage";
import logo from "../../assets/images/hivemind-logo.png";

export default function OAuthSuccessPage() {
  const { t } = useTranslation();
  useOAuthSuccessPage();

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-surface-soft px-6 text-center text-app">
      <div className="rounded-card bg-surface p-8 shadow-lg">
        <img src={logo} alt="HiveMind" className="mx-auto h-12 w-12 object-contain" />
      </div>
      <svg className="mt-8 h-10 w-10 animate-spin text-primary" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeOpacity="0.2" strokeWidth="4" />
        <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
      </svg>
      <h1 className="mt-6 text-2xl font-bold tracking-tight">{t("auth.oauth.signingIn")}</h1>
      <p className="mt-2 max-w-sm text-secondary">{t("auth.oauth.preparingSpace")}</p>
    </div>
  );
}
