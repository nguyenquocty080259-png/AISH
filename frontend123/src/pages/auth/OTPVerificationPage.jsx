import { useState, useRef, useEffect } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { verifyOtp, resendOtp } from "../../services/authService";
import "./AuthPage.css";

const OTP_LENGTH = 6;
const RESEND_COUNTDOWN = 120; // seconds

export default function OTPVerificationPage() {
  const navigate  = useNavigate();
  const location  = useLocation();
  const email     = location.state?.email || "";

  const [otp, setOtp]         = useState(Array(OTP_LENGTH).fill(""));
  const [error, setError]     = useState("");
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(RESEND_COUNTDOWN);
  const [resending, setResending] = useState(false);

  const inputRefs = useRef([]);

  // Redirect nếu không có email
  useEffect(() => {
    if (!email) navigate("/register", { replace: true });
  }, [email, navigate]);

  // Countdown resend
  useEffect(() => {
    if (countdown <= 0) return;
    const timer = setInterval(() => setCountdown((c) => c - 1), 1000);
    return () => clearInterval(timer);
  }, [countdown]);

  const handleChange = (index, value) => {
    if (!/^\d*$/.test(value)) return; // chỉ nhận số

    const newOtp = [...otp];
    newOtp[index] = value.slice(-1); // chỉ lấy 1 ký tự
    setOtp(newOtp);
    setError("");

    // Auto focus ô tiếp theo
    if (value && index < OTP_LENGTH - 1) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === "Backspace" && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, OTP_LENGTH);
    if (!pasted) return;

    const newOtp = [...otp];
    pasted.split("").forEach((char, i) => {
      newOtp[i] = char;
    });
    setOtp(newOtp);
    inputRefs.current[Math.min(pasted.length, OTP_LENGTH - 1)]?.focus();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const otpCode = otp.join("");

    if (otpCode.length < OTP_LENGTH) {
      setError("Vui lòng nhập đủ 6 chữ số OTP");
      return;
    }

    setLoading(true);
    setError("");

    try {
      await verifyOtp({ email, otp: otpCode });
      navigate("/login", { state: { verified: true } });
    } catch (err) {
      setError(err.message || "OTP không hợp lệ hoặc đã hết hạn");
      setOtp(Array(OTP_LENGTH).fill(""));
      inputRefs.current[0]?.focus();
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResending(true);
    setError("");

    try {
      await resendOtp(email);
      setCountdown(RESEND_COUNTDOWN);
      setOtp(Array(OTP_LENGTH).fill(""));
      inputRefs.current[0]?.focus();
    } catch (err) {
      setError(err.message || "Gửi lại OTP thất bại");
    } finally {
      setResending(false);
    }
  };

  const formatCountdown = (s) =>
    `${String(Math.floor(s / 60)).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;

  return (
    <div className="auth-page">
      <div className="auth-bg">
        <div className="auth-bg__blob auth-bg__blob--1" />
        <div className="auth-bg__blob auth-bg__blob--2" />
      </div>

      <div className="auth-card">
        <div className="auth-card__logo">
          <span className="auth-card__logo-icon">✦</span>
          <span className="auth-card__logo-text">AISH</span>
        </div>

        <h1 className="auth-card__title">Xác thực email</h1>
        <p className="auth-card__subtitle">
          Nhập mã OTP gồm 6 chữ số đã gửi đến{" "}
          <strong>{email}</strong>
        </p>

        <form className="auth-form" onSubmit={handleSubmit}>
          {/* OTP inputs */}
          <div className="otp-group" onPaste={handlePaste}>
            {otp.map((digit, index) => (
              <input
                key={index}
                ref={(el) => (inputRefs.current[index] = el)}
                type="text"
                inputMode="numeric"
                maxLength={1}
                className="otp-group__input"
                value={digit}
                onChange={(e) => handleChange(index, e.target.value)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                autoFocus={index === 0}
              />
            ))}
          </div>

          {error && <p className="auth-form__error">{error}</p>}

          <button
            type="submit"
            className="auth-form__btn"
            disabled={loading}
          >
            {loading ? "Đang xác thực..." : "Xác thực"}
          </button>
        </form>

        {/* Resend */}
        <div className="auth-resend">
          {countdown > 0 ? (
            <span>Gửi lại sau {formatCountdown(countdown)}</span>
          ) : (
            <button
              className="auth-resend__btn"
              onClick={handleResend}
              disabled={resending}
            >
              {resending ? "Đang gửi..." : "Gửi lại OTP"}
            </button>
          )}
        </div>

        <p className="auth-card__footer">
          <Link to="/register" className="auth-card__link">← Quay lại đăng ký</Link>
        </p>
      </div>
    </div>
  );
}