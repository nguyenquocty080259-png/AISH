import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as authApi from "../../../api/authApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

// Hook logic trang Đăng ký: gửi form, thành công thì điều hướng sang trang nhập OTP xác minh email.
export function useSignUpPage() {
  const { t } = useTranslation();
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // Gọi API POST /auth/signup rồi điều hướng sang trang OTP kèm email vừa đăng ký.
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await authApi.signup({ fullName, email, password });
      showSuccess(t("auth.signup.success"));
      navigate(ROUTES.VERIFY_OTP, { state: { email } });
    } catch (err) {
      showError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return {
    fullName,
    setFullName,
    email,
    setEmail,
    password,
    setPassword,
    submitting,
    handleSubmit,
  };
}
