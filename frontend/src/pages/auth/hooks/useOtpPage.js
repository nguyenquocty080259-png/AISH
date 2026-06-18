import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import * as authApi from "../../../api/authApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

const RESEND_COOLDOWN_SECONDS = 60;

export function useOtpPage() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  // Trang này chỉ có ý nghĩa khi tới từ SignUp/Login với email kèm theo.
  const email = state?.email ?? "";

  const [otp, setOtp] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [resending, setResending] = useState(false);
  const [cooldown, setCooldown] = useState(RESEND_COOLDOWN_SECONDS);

  useEffect(() => {
    if (!email) return;
    if (cooldown <= 0) return;
    const timer = setInterval(() => {
      setCooldown((prev) => prev - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldown, email]);

  const handleVerify = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await authApi.verifyOtp({ email, otp });
      showSuccess("Xác minh email thành công, vui lòng đăng nhập.");
      navigate(ROUTES.LOGIN);
    } catch (err) {
      showError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleResend = async () => {
    setResending(true);
    try {
      await authApi.resendOtp({ email });
      showSuccess("Đã gửi lại mã OTP, vui lòng kiểm tra email.");
      setCooldown(RESEND_COOLDOWN_SECONDS);
    } catch (err) {
      showError(err.message);
    } finally {
      setResending(false);
    }
  };

  return {
    email,
    otp,
    setOtp,
    submitting,
    resending,
    cooldown,
    handleVerify,
    handleResend,
  };
}
