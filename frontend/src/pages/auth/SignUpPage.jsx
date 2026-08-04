import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useSignUpPage } from "./hooks/useSignUpPage";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";
import PasswordField from "../../components/auth/PasswordField";
import SocialButtons from "../../components/auth/SocialButtons";
import Button from "../../components/ui/Button";
import { Input } from "../../components/ui/Field";

// Trang ĐĂNG KÝ: form họ tên + email + mật khẩu, và nút đăng ký nhanh qua Google/GitHub.
export default function SignUpPage() {
  const { t } = useTranslation();
  const { fullName, setFullName, email, setEmail, password, setPassword, submitting, handleSubmit } = useSignUpPage();

  return (
    <AuthShell>
      <h1 className="auth-title">{t("auth.signup.title")}</h1>
      <p className="auth-subtitle">{t("auth.signup.subtitle")}</p>

      <form className="auth-form" onSubmit={handleSubmit}>
        <Input
          id="fullName"
          type="text"
          label={t("auth.signup.fullName")}
          required
          autoComplete="name"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          placeholder={t("auth.signup.fullNamePlaceholder")}
        />

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

        <PasswordField
          id="password"
          label={t("auth.signup.password")}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          minLength={8}
          helper={t("auth.signup.passwordHelper")}
        />

        <Button type="submit" size="lg" block loading={submitting} className="auth-form__submit">
          {submitting ? t("auth.signup.submitting") : t("auth.signup.submit")}
        </Button>
      </form>

      <div className="auth-divider">{t("auth.orDivider")}</div>

      <SocialButtons />

      <p className="auth-foot">
        {t("auth.signup.haveAccount")}{" "}
        <Link to={ROUTES.LOGIN} className="auth-link has-custom-focus">{t("auth.signup.login")}</Link>
      </p>
    </AuthShell>
  );
}
