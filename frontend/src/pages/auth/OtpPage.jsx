import { Link } from "react-router-dom";
import { useOtpPage } from "./hooks/useOtpPage";
import { ROUTES } from "../../constants/routes";
import "./auth.css";

export default function OtpPage() {
  const {
    email,
    otp,
    setOtp,
    submitting,
    resending,
    cooldown,
    handleVerify,
    handleResend,
  } = useOtpPage();

  if (!email) {
    return (
      <div className="auth-page">
        <div className="auth-card">
          <h1 className="auth-card__title">Thiếu thông tin email</h1>
          <p className="auth-card__subtitle">
            Vui lòng đăng ký hoặc đăng nhập lại để nhận mã OTP.
          </p>
          <Link to={ROUTES.SIGNUP} className="auth-resend">
            Quay lại đăng ký
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="auth-card__brand">AISH</p>
        <h1 className="auth-card__title">Xác minh email</h1>
        <p className="auth-card__subtitle">
          Mã OTP gồm 6 số đã được gửi tới <strong>{email}</strong>. Mã có hiệu
          lực trong 2 phút.
        </p>

        <form className="auth-form" onSubmit={handleVerify}>
          <div className="auth-field">
            <label htmlFor="otp">Mã OTP</label>
            <input
              id="otp"
              className="auth-otp-input"
              type="text"
              inputMode="numeric"
              maxLength={6}
              required
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, ""))}
              placeholder="000000"
            />
          </div>

          <button className="auth-submit" type="submit" disabled={submitting}>
            {submitting ? "Đang xác minh..." : "Xác minh"}
          </button>
        </form>

        <p className="auth-footer">
          Không nhận được mã?{" "}
          <button
            className="auth-resend"
            type="button"
            disabled={cooldown > 0 || resending}
            onClick={handleResend}
          >
            {cooldown > 0 ? `Gửi lại sau ${cooldown}s` : "Gửi lại mã OTP"}
          </button>
        </p>
      </div>
    </div>
  );
}
