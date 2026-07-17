import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import * as profileApi from "../../../api/profileApi";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

export function useOnboardingPage() {
  const { refreshProfile } = useAuth();
  const { showError, showSuccess } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [dob, setDob] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await profileApi.completeOnboarding({ dob });
      await refreshProfile();
      showSuccess("Đã lưu thông tin, chào mừng bạn!");
      const redirectTo = location.state?.from?.pathname ?? ROUTES.DASHBOARD;
      navigate(redirectTo, { replace: true });
    } catch (err) {
      showError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return {
    dob,
    setDob,
    submitting,
    handleSubmit,
  };
}
