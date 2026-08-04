import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useLoginPage } from "./hooks/useLoginPage";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";
import PasswordField from "../../components/auth/PasswordField";
import SocialButtons from "../../components/auth/SocialButtons";
import Button from "../../components/ui/Button";
import { Input } from "../../components/ui/Field";

// Trang ĐĂNG NHẬP: form email + mật khẩu, link quên mật khẩu, và nút đăng nhập Google/GitHub.
export default function LoginPage() {
  const { t } = useTranslation();
  const { email, setEmail, password, setPassword, submitting, handleSubmit } = useLoginPage();

  return (
    <AuthShell>
      <h1 className="auth-title">{t("auth.login.title")}</h1>
      <p className="auth-subtitle">{t("auth.login.subtitle")}</p>

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

        {/* Label tự render ở đây để đặt link "Quên mật khẩu?" cùng hàng với nhãn. */}
        <div>
          <div className="auth-label-row">
            <label htmlFor="password" className="ui-field__label">{t("auth.login.password")}</label>
            <Link to={ROUTES.FORGOT_PASSWORD} className="auth-link has-custom-focus">
              {t("auth.login.forgotPassword")}
            </Link>
          </div>
          <PasswordField
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
          />
        </div>

        <Button type="submit" size="lg" block loading={submitting} className="auth-form__submit">
          {submitting ? t("auth.login.submitting") : t("auth.login.submit")}
        </Button>
      </form>

      <div className="auth-divider">{t("auth.orDivider")}</div>

      <SocialButtons />

      <p className="auth-foot">
        {t("auth.login.noAccount")}{" "}
        <Link to={ROUTES.SIGNUP} className="auth-link has-custom-focus">{t("auth.login.signupNow")}</Link>
      </p>
    </AuthShell>
  );
}
