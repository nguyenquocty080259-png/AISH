import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as authApi from "../../api/authApi";
import { useToast } from "../../hooks/useToast";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";
import Button from "../../components/ui/Button";
import { Input } from "../../components/ui/Field";

// Trang bắt đầu luồng QUÊN MẬT KHẨU: nhập email, gửi OTP, rồi chuyển sang trang OTP.
export default function ForgotPasswordPage() {
  const { t } = useTranslation();
  const { showError } = useToast();
  const [email, setEmail] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  // Gọi API POST /auth/forgot-password rồi điều hướng sang trang OTP với mode="forgot-password"
  // (OtpPage đọc mode này để biết cần gọi verifyForgotPassword thay vì verifyOtp).
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
      <h1 className="auth-title">{t("auth.forgot.title")}</h1>
      <p className="auth-subtitle">{t("auth.forgot.subtitle")}</p>

      <form className="auth-form" onSubmit={handleSubmit}>
        <Input
          id="email"
          type="email"
          label={t("auth.login.email")}
          required
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder={t("auth.login.emailPlaceholder")}
        />

        <Button type="submit" size="lg" block loading={submitting}>
          {submitting ? t("auth.forgot.submitting") : t("auth.forgot.submit")}
        </Button>
      </form>

      <p className="auth-foot">
        <Link to={ROUTES.LOGIN} className="auth-link has-custom-focus">{t("auth.forgot.backToLogin")}</Link>
      </p>
    </AuthShell>
  );
}
