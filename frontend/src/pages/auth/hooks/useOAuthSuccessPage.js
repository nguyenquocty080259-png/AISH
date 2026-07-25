import { useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";
import { ROLES } from "../../../constants/roles";

// BE (OAuth2SuccessHandler) redirect về đây với ?token=<jwt> sau khi social login thành
// công. Chỉ có MỘT token (không có refresh token riêng cho luồng OAuth) - lưu qua
// loginWithToken rồi để PrivateRoute tự lo tiếp (kể cả bắt onboarding nếu dob=null).
export function useOAuthSuccessPage() {
  const { t } = useTranslation();
  const { loginWithToken } = useAuth();
  const { showError } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const handledRef = useRef(false);

  useEffect(() => {
    if (handledRef.current) return;
    handledRef.current = true;

    const token = searchParams.get("token");
    if (!token) {
      showError(t("auth.oauth.failed"));
      navigate(ROUTES.LOGIN, { replace: true });
      return;
    }

    loginWithToken(token)
      .then((profile) => {
        navigate(profile.role === ROLES.ADMIN ? ROUTES.ADMIN : ROUTES.DASHBOARD, {
          replace: true,
        });
      })
      .catch(() => {
        showError(t("auth.oauth.failed"));
        navigate(ROUTES.LOGIN, { replace: true });
      });
  }, [searchParams, loginWithToken, navigate, showError, t]);
}
