import { useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as authApi from "../../api/authApi";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";
import PasswordField from "../../components/auth/PasswordField";

export default function ResetPasswordPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { state } = useLocation();
  const resetToken = state?.resetToken ?? "";
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!resetToken) { setError(t("auth.reset.expired")); return; }
    if (password.length < 8) { setError(t("auth.reset.tooShort")); return; }
    if (password !== confirmPassword) { setError(t("auth.reset.mismatch")); return; }
    try {
      setSubmitting(true);
      await authApi.resetPassword({ resetToken, password });
      alert(t("auth.reset.success"));
      navigate(ROUTES.LOGIN);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const lengthOk = password.length >= 8;

  return (
    <AuthShell>
      <h1 className="text-3xl font-bold tracking-tight text-app">{t("auth.reset.title")}</h1>
      <p className="mt-2 text-secondary">{t("auth.reset.subtitle")}</p>

      <form className="mt-8 flex flex-col gap-5" onSubmit={handleSubmit}>
        <PasswordField id="password" label={t("auth.reset.newPassword")} value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />
        <PasswordField id="confirmPassword" label={t("auth.reset.confirmPassword")} value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="••••••••" />

        <div className="rounded-input bg-surface-soft px-4 py-3 text-sm">
          <p className="font-semibold text-secondary">{t("auth.reset.requirementsTitle")}</p>
          <p className={lengthOk ? "mt-1 text-primary-dark" : "mt-1 text-secondary"}>{lengthOk ? "✓" : "○"} {t("auth.reset.minChars")}</p>
        </div>

        {error && <p className="rounded-input bg-error px-4 py-2.5 text-sm text-white">{error}</p>}

        <button type="submit" disabled={submitting}
          className="mt-1 w-full rounded-input bg-primary py-3 font-semibold text-white transition-colors hover:bg-primary-dark disabled:opacity-60">
          {submitting ? t("auth.reset.submitting") : t("auth.reset.submit")}
        </button>
      </form>

      <div className="mt-6 text-center">
        <Link to={ROUTES.LOGIN} className="text-sm font-semibold text-secondary hover:text-primary">{t("auth.reset.backToLogin")}</Link>
      </div>
    </AuthShell>
  );
}
