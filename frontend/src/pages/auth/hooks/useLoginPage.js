import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

export function useLoginPage() {
  const { login } = useAuth();
  const { showError } = useToast();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await login({ email, password });
      navigate(ROUTES.DASHBOARD);
    } catch (err) {
      // Backend báo "Please verify your email first" khi account chưa verify OTP
      if (err.message?.toLowerCase().includes("verify")) {
        showError("Email chưa được xác minh, vui lòng xác minh OTP.");
        navigate(ROUTES.VERIFY_OTP, { state: { email } });
      } else {
        showError(err.message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  return {
    email,
    setEmail,
    password,
    setPassword,
    submitting,
    handleSubmit,
  };
}
