import { useRef } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useOtpPage } from "./hooks/useOtpPage";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";
import Button from "../../components/ui/Button";

// Trang nhập OTP: 6 ô nhập số riêng biệt (tự nhảy ô, hỗ trợ dán cả mã cùng lúc), dùng chung cho
// xác minh đăng ký và xác minh quên mật khẩu.
export default function OtpPage() {
  const { t } = useTranslation();
  const { email, otp, setOtp, submitting, resending, cooldown, handleVerify, handleResend } = useOtpPage();
  const inputsRef = useRef([]);

  if (!email) {
    return (
      <AuthShell>
        <h1 className="auth-title">{t("auth.otp.missingEmailTitle")}</h1>
        <p className="auth-subtitle">{t("auth.otp.missingEmailSubtitle")}</p>
        <p className="auth-foot">
          <Link to={ROUTES.SIGNUP} className="auth-link has-custom-focus">{t("auth.otp.backToSignup")}</Link>
        </p>
      </AuthShell>
    );
  }

  const digits = otp.padEnd(6, " ").slice(0, 6).split("");

  const setDigit = (i, val) => {
    const clean = val.replace(/\D/g, "");
    const arr = otp.padEnd(6, " ").slice(0, 6).split("");
    if (clean === "") { arr[i] = " "; setOtp(arr.join("").replace(/ /g, "")); return; }
    arr[i] = clean[clean.length - 1];
    setOtp(arr.join("").replace(/ /g, ""));
    if (i < 5) inputsRef.current[i + 1]?.focus();
  };

  const onKeyDown = (i, e) => {
    if (e.key === "Backspace" && !digits[i].trim() && i > 0) inputsRef.current[i - 1]?.focus();
  };

  const onPaste = (e) => {
    e.preventDefault();
    const text = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
    setOtp(text);
    inputsRef.current[Math.min(text.length, 5)]?.focus();
  };

  return (
    <AuthShell>
      <div className="auth-icon" aria-hidden="true">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
      </div>

      <h1 className="auth-title">{t("auth.otp.title")}</h1>
      <p className="auth-subtitle">
        {t("auth.otp.sentPrefix")} <strong>{email}</strong>. {t("auth.otp.sentSuffix")}
      </p>

      <form className="auth-form" onSubmit={handleVerify}>
        <div>
          <span className="ui-field__label" id="otp-label">{t("auth.otp.label")}</span>
          <div className="auth-otp" onPaste={onPaste} role="group" aria-labelledby="otp-label">
            {digits.map((d, i) => (
              <input
                key={i}
                ref={(el) => (inputsRef.current[i] = el)}
                type="text"
                inputMode="numeric"
                autoComplete={i === 0 ? "one-time-code" : "off"}
                maxLength={1}
                aria-label={t("auth.otp.digitAria", { index: i + 1 })}
                value={d.trim()}
                onChange={(e) => setDigit(i, e.target.value)}
                onKeyDown={(e) => onKeyDown(i, e)}
              />
            ))}
          </div>
        </div>

        <Button type="submit" size="lg" block loading={submitting} disabled={otp.length < 6}>
          {submitting ? t("auth.otp.submitting") : t("auth.otp.submit")}
        </Button>
      </form>

      <p className="auth-foot">
        {t("auth.otp.notReceived")}{" "}
        <Button variant="link" size="sm" disabled={cooldown > 0 || resending} onClick={handleResend}>
          {cooldown > 0 ? t("auth.otp.resendIn", { count: cooldown }) : t("auth.otp.resend")}
        </Button>
      </p>

      <p className="auth-foot auth-foot--divided">
        <Link to={ROUTES.SIGNUP} className="auth-link has-custom-focus">{t("auth.otp.backToSignupPage")}</Link>
      </p>
    </AuthShell>
  );
}
