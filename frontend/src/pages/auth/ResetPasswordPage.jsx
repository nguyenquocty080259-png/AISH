import { useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as authApi from "../../api/authApi";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";
import PasswordField from "../../components/auth/PasswordField";
import Button from "../../components/ui/Button";
import Alert from "../../components/ui/Alert";

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
      <h1 className="auth-title">{t("auth.reset.title")}</h1>
      <p className="auth-subtitle">{t("auth.reset.subtitle")}</p>

      <form className="auth-form" onSubmit={handleSubmit}>
        <PasswordField id="password" label={t("auth.reset.newPassword")} value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />
        <PasswordField id="confirmPassword" label={t("auth.reset.confirmPassword")} value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="••••••••" />

        <div className="auth-rules">
          <p className="auth-rules__title">{t("auth.reset.requirementsTitle")}</p>
          <p className={`auth-rules__item ${lengthOk ? "auth-rules__item--ok" : ""}`.trim()}>
            <span aria-hidden="true">{lengthOk ? "✓" : "○"}</span> {t("auth.reset.minChars")}
          </p>
        </div>

        {error && <Alert intent="danger">{error}</Alert>}

        <Button type="submit" size="lg" block loading={submitting} className="auth-form__submit">
          {submitting ? t("auth.reset.submitting") : t("auth.reset.submit")}
        </Button>
      </form>

      <p className="auth-foot">
        <Link to={ROUTES.LOGIN} className="auth-link has-custom-focus">{t("auth.reset.backToLogin")}</Link>
      </p>
    </AuthShell>
  );
}
