import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";
import { ROLES } from "../../../constants/roles";

// Mã lỗi OAuth2SuccessHandler gắn vào ?error= khi redirect về /login. Trường hợp
// "email đã đăng ký local" BE tự gửi message tiếng Việt đầy đủ (đã URL-encode) nên
// không nằm trong map này - rơi vào nhánh mặc định và hiển thị nguyên văn.
const OAUTH_ERROR_MESSAGES = {
  no_email: "Không lấy được email từ tài khoản mạng xã hội. Vui lòng dùng email công khai hoặc đăng nhập bằng phương thức khác.",
  banned: "Tài khoản của bạn đã bị khóa.",
  pending: "Tài khoản của bạn đang chờ phê duyệt.",
};

export function useLoginPage() {
  const { login } = useAuth();
  const { showError } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const error = searchParams.get("error");
    if (!error) return;
    showError(OAUTH_ERROR_MESSAGES[error] ?? error);
    navigate(ROUTES.LOGIN, { replace: true });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const profile = await login({ email, password });
      navigate(profile.role === ROLES.ADMIN ? ROUTES.ADMIN : ROUTES.DASHBOARD);
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
