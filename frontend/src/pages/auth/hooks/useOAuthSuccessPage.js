import { useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";
import { ROLES } from "../../../constants/roles";

const OAUTH_FAILED_MESSAGE = "Đăng nhập bằng mạng xã hội thất bại, vui lòng thử lại.";

// BE (OAuth2SuccessHandler) redirect về đây với ?token=<jwt> sau khi social login thành
// công. Chỉ có MỘT token (không có refresh token riêng cho luồng OAuth) - lưu qua
// loginWithToken rồi để PrivateRoute tự lo tiếp (kể cả bắt onboarding nếu dob=null).
export function useOAuthSuccessPage() {
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
      showError(OAUTH_FAILED_MESSAGE);
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
        showError(OAUTH_FAILED_MESSAGE);
        navigate(ROUTES.LOGIN, { replace: true });
      });
  }, [searchParams, loginWithToken, navigate, showError]);
}
