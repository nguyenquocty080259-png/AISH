import { useState } from "react";
import { useNavigate } from "react-router-dom";
import * as authApi from "../../../api/authApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

export function useSignUpPage() {
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await authApi.signup({ fullName, email, password });
      showSuccess("Đăng ký thành công, vui lòng kiểm tra email để lấy mã OTP.");
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
