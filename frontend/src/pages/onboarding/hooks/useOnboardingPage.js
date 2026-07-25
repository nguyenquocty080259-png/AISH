import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as profileApi from "../../../api/profileApi";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

// dob luôn bắt buộc; fullName bắt buộc trên FE để phản hồi tức thì, nhưng BE mới là nguồn
// sự thật cuối cùng (user OAuth đã có fullName thì onboarding có thể bỏ trống lại được).
const REQUIRED_FIELDS = ["fullName", "dob"];

const OPTIONAL_FIELDS = [
  "bio",
  "gender",
  "phoneNumber",
  "university",
  "faculty",
  "major",
  "country",
  "city",
  "githubUrl",
  "linkedinUrl",
  "websiteUrl",
];

const ALL_FIELDS = [...REQUIRED_FIELDS, ...OPTIONAL_FIELDS];

function toFormValues(profile) {
  const values = {};
  ALL_FIELDS.forEach((field) => {
    values[field] = profile?.[field] ?? "";
  });
  return values;
}

export function useOnboardingPage() {
  const { t } = useTranslation();
  const { refreshProfile } = useAuth();
  const { showError, showSuccess } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState(toFormValues(null));
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let cancelled = false;
    profileApi
      .getMyProfile()
      .then((profile) => {
        if (!cancelled) {
          setForm(toFormValues(profile));
        }
      })
      .catch(() => {
        // Không tải được hồ sơ để prefill thì vẫn cho người dùng điền từ đầu.
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const handleFieldChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!form.fullName?.trim()) {
      showError(t("onboarding.fullNameRequired"));
      return;
    }
    if (!form.dob) {
      showError(t("onboarding.dobRequired"));
      return;
    }

    setSubmitting(true);
    try {
      // Chỉ gửi field đã điền - onboarding là partial-update, field bỏ trống sẽ không
      // ghi đè giá trị đã có ở BE.
      const payload = { dob: form.dob, fullName: form.fullName.trim() };
      OPTIONAL_FIELDS.forEach((field) => {
        const value = form[field];
        if (typeof value === "string" && value.trim() !== "") {
          payload[field] = value.trim();
        }
      });

      await profileApi.completeOnboarding(payload);
      await refreshProfile();
      showSuccess(t("onboarding.welcome"));
      const redirectTo = location.state?.from?.pathname ?? ROUTES.DASHBOARD;
      navigate(redirectTo, { replace: true });
    } catch (err) {
      showError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return {
    form,
    loading,
    submitting,
    handleFieldChange,
    handleSubmit,
  };
}

export { REQUIRED_FIELDS, OPTIONAL_FIELDS };
