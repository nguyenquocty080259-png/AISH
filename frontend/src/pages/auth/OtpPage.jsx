import { useRef } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useOtpPage } from "./hooks/useOtpPage";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";

export default function OtpPage() {
  const { t } = useTranslation();
  const { email, otp, setOtp, submitting, resending, cooldown, handleVerify, handleResend } = useOtpPage();
  const inputsRef = useRef([]);

  if (!email) {
    return (
      <AuthShell>
        <h1 className="text-3xl font-bold tracking-tight text-app">{t("auth.otp.missingEmailTitle")}</h1>
        <p className="mt-2 text-secondary">{t("auth.otp.missingEmailSubtitle")}</p>
        <Link to={ROUTES.SIGNUP} className="mt-6 inline-block font-semibold text-primary hover:text-primary-dark">{t("auth.otp.backToSignup")}</Link>
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
      <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-input bg-surface-soft text-primary">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
      </div>
      <h1 className="text-3xl font-bold tracking-tight text-app">{t("auth.otp.title")}</h1>
      <p className="mt-2 text-secondary">{t("auth.otp.sentPrefix")} <strong className="text-app">{email}</strong>. {t("auth.otp.sentSuffix")}</p>

      <form className="mt-8" onSubmit={handleVerify}>
        <label className="text-sm font-semibold text-secondary">{t("auth.otp.label")}</label>
        <div className="mt-2 flex justify-between gap-2" onPaste={onPaste}>
          {digits.map((d, i) => (
            <input key={i} ref={(el) => (inputsRef.current[i] = el)} type="text" inputMode="numeric" maxLength={1}
              value={d.trim()} onChange={(e) => setDigit(i, e.target.value)} onKeyDown={(e) => onKeyDown(i, e)}
              className="h-14 w-full rounded-input border border-border bg-surface text-center text-2xl font-semibold text-app outline-none focus:border-primary" />
          ))}
        </div>

        <button type="submit" disabled={submitting || otp.length < 6}
          className="mt-6 w-full rounded-input bg-primary py-3 font-semibold text-white transition-colors hover:bg-primary-dark disabled:opacity-60">
          {submitting ? t("auth.otp.submitting") : t("auth.otp.submit")}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-secondary">
        {t("auth.otp.notReceived")}{" "}
        <button type="button" disabled={cooldown > 0 || resending} onClick={handleResend}
          className="font-semibold text-primary hover:text-primary-dark disabled:text-secondary">
          {cooldown > 0 ? t("auth.otp.resendIn", { count: cooldown }) : t("auth.otp.resend")}
        </button>
      </p>

      <div className="mt-6 border-t border-border pt-6 text-center">
        <Link to={ROUTES.SIGNUP} className="text-sm font-semibold text-secondary hover:text-primary">{t("auth.otp.backToSignupPage")}</Link>
      </div>
    </AuthShell>
  );
}
