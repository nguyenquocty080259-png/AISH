import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useSignUpPage } from "./hooks/useSignUpPage";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";
import PasswordField from "../../components/auth/PasswordField";
import SocialButtons from "../../components/auth/SocialButtons";

export default function SignUpPage() {
  const { t } = useTranslation();
  const { fullName, setFullName, email, setEmail, password, setPassword, submitting, handleSubmit } = useSignUpPage();

  return (
    <AuthShell>
      <h1 className="text-3xl font-bold tracking-tight text-app">{t("auth.signup.title")}</h1>
      <p className="mt-2 text-secondary">{t("auth.signup.subtitle")}</p>

      <form className="mt-8 flex flex-col gap-5" onSubmit={handleSubmit}>
        <div className="flex flex-col gap-1.5">
          <label htmlFor="fullName" className="text-sm font-semibold text-secondary">{t("auth.signup.fullName")}</label>
          <input id="fullName" type="text" required value={fullName} onChange={(e) => setFullName(e.target.value)} placeholder={t("auth.signup.fullNamePlaceholder")}
            className="w-full rounded-input border border-border bg-surface px-4 py-2.5 text-app outline-none focus:border-primary" />
        </div>
        <div className="flex flex-col gap-1.5">
          <label htmlFor="email" className="text-sm font-semibold text-secondary">{t("auth.login.email")}</label>
          <input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder={t("auth.login.emailPlaceholder")}
            className="w-full rounded-input border border-border bg-surface px-4 py-2.5 text-app outline-none focus:border-primary" />
        </div>
        <PasswordField id="password" label={t("auth.signup.password")} value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" minLength={8} helper={t("auth.signup.passwordHelper")} />

        <button type="submit" disabled={submitting}
          className="mt-1 w-full rounded-input bg-primary py-3 font-semibold text-white transition-colors hover:bg-primary-dark disabled:opacity-60">
          {submitting ? t("auth.signup.submitting") : t("auth.signup.submit")}
        </button>
      </form>

      <div className="my-6 flex items-center gap-4">
        <span className="flex-1 border-t border-border" />
        <span className="text-sm text-secondary">{t("auth.orDivider")}</span>
        <span className="flex-1 border-t border-border" />
      </div>

      <SocialButtons />

      <p className="mt-6 text-center text-sm text-secondary">
        {t("auth.signup.haveAccount")} <Link to={ROUTES.LOGIN} className="font-semibold text-primary hover:text-primary-dark">{t("auth.signup.login")}</Link>
      </p>
    </AuthShell>
  );
}
