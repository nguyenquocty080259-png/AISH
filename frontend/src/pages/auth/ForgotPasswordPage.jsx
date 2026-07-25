import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as authApi from "../../api/authApi";
import { useToast } from "../../hooks/useToast";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";

export default function ForgotPasswordPage() {
  const { t } = useTranslation();
  const { showError } = useToast();
  const [email, setEmail] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await authApi.forgotPassword({ email });
      navigate(ROUTES.VERIFY_OTP, { state: { email, mode: "forgot-password" } });
    } catch (err) {
      showError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell>
      <h1 className="text-3xl font-bold tracking-tight text-app">{t("auth.forgot.title")}</h1>
      <p className="mt-2 text-secondary">{t("auth.forgot.subtitle")}</p>

      <form className="mt-8 flex flex-col gap-5" onSubmit={handleSubmit}>
        <div className="flex flex-col gap-1.5">
          <label htmlFor="email" className="text-sm font-semibold text-secondary">{t("auth.login.email")}</label>
          <input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder={t("auth.login.emailPlaceholder")}
            className="w-full rounded-input border border-border bg-surface px-4 py-2.5 text-app outline-none focus:border-primary" />
        </div>
        <button type="submit" disabled={submitting}
          className="w-full rounded-input bg-primary py-3 font-semibold text-white transition-colors hover:bg-primary-dark disabled:opacity-60">
          {submitting ? t("auth.forgot.submitting") : t("auth.forgot.submit")}
        </button>
      </form>

      <div className="mt-6 text-center">
        <Link to={ROUTES.LOGIN} className="text-sm font-semibold text-secondary hover:text-primary">{t("auth.forgot.backToLogin")}</Link>
      </div>
    </AuthShell>
  );
}
