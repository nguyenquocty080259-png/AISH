import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as authApi from "../../../api/authApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

const RESEND_COOLDOWN_SECONDS = 60;

// Hook logic trang nhập OTP — dùng chung cho 2 luồng: xác minh email lúc đăng ký (mode="signup")
// và xác minh OTP quên mật khẩu (mode="forgot-password"). Có đếm ngược 60 giây trước khi cho gửi
// lại OTP để tránh spam.
export function useOtpPage() {

  const { t } = useTranslation();

  const { state } = useLocation();

  const navigate = useNavigate();

  const { showSuccess, showError } = useToast();

  const email = state?.email ?? "";

  const mode = state?.mode ?? "signup";

  const [otp, setOtp] = useState("");

  const [submitting, setSubmitting] = useState(false);

  const [resending, setResending] = useState(false);

  const [cooldown, setCooldown] =
    useState(RESEND_COOLDOWN_SECONDS);
  useEffect(() => {
    if (!email) return;
    if (cooldown <= 0) return;
    const timer = setInterval(() => {
      setCooldown(prev => prev - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldown, email]);
  // Xác minh OTP. mode="forgot-password" -> gọi verifyForgotPassword, lấy resetToken rồi sang
  // trang đặt mật khẩu mới; mode="signup" (mặc định) -> gọi verifyOtp, xong thì về trang đăng nhập.
  const handleVerify = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      if (mode === "forgot-password") {
        const response = await authApi.verifyForgotPassword({
          email,
          otp,
        });
        showSuccess(
          t("auth.otp.verifySuccess")
        );
        navigate(
          ROUTES.RESET_PASSWORD,
          {
            state: {
              resetToken:
                response.data.resetToken,
            },
          }
        );
        return;
      }
      await authApi.verifyOtp({
        email,
        otp
      });
      showSuccess(
        t("auth.otp.verifyEmailSuccess")
      );
      navigate(
        ROUTES.LOGIN
      );
    } catch (err) {
      showError(
        err.message
      );
    } finally {
      setSubmitting(false);
    }
  };

  // Gửi lại OTP mới, đúng API theo mode hiện tại, rồi khởi động lại đếm ngược.
  const handleResend = async () => {
    setResending(true);
    try {
      if (
        mode ===
        "forgot-password"
      ) {
        await authApi.forgotPassword({
          email,
        });
      } else {
        await authApi.resendOtp({
          email
        });
      }
      showSuccess(
        t("auth.otp.resent")
      );
      setCooldown(
        RESEND_COOLDOWN_SECONDS
      );
    } catch (err) {
      showError(
        err.message
      );
    } finally {
      setResending(false);
    }
  };

  return {
    email,
    mode,
    otp,
    setOtp,
    submitting,
    resending,
    cooldown,
    handleVerify,
    handleResend,
  };
}