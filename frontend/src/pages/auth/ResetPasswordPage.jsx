import { useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import * as authApi from "../../api/authApi";
import { ROUTES } from "../../constants/routes";
import AuthShell from "../../components/auth/AuthShell";
import PasswordField from "../../components/auth/PasswordField";

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const resetToken = state?.resetToken ?? "";
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!resetToken) { setError("Phiên đặt lại mật khẩu đã hết hạn. Vui lòng thực hiện lại."); return; }
    if (password.length < 8) { setError("Mật khẩu phải có ít nhất 8 ký tự."); return; }
    if (password !== confirmPassword) { setError("Mật khẩu xác nhận không khớp."); return; }
    try {
      setSubmitting(true);
      await authApi.resetPassword({ resetToken, password });
      alert("Đổi mật khẩu thành công.");
      navigate(ROUTES.LOGIN);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const lengthOk = password.length >= 8;

  return (
    <AuthShell>
      <h1 className="text-3xl font-bold tracking-tight text-app">Đặt lại mật khẩu</h1>
      <p className="mt-2 text-secondary">Tạo mật khẩu mới cho tài khoản.</p>

      <form className="mt-8 flex flex-col gap-5" onSubmit={handleSubmit}>
        <PasswordField id="password" label="Mật khẩu mới" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />
        <PasswordField id="confirmPassword" label="Xác nhận mật khẩu" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="••••••••" />

        <div className="rounded-input bg-surface-soft px-4 py-3 text-sm">
          <p className="font-semibold text-secondary">Yêu cầu mật khẩu:</p>
          <p className={lengthOk ? "mt-1 text-primary-dark" : "mt-1 text-secondary"}>{lengthOk ? "✓" : "○"} Tối thiểu 8 ký tự</p>
        </div>

        {error && <p className="rounded-input bg-error px-4 py-2.5 text-sm text-white">{error}</p>}

        <button type="submit" disabled={submitting}
          className="mt-1 w-full rounded-input bg-primary py-3 font-semibold text-white transition-colors hover:bg-primary-dark disabled:opacity-60">
          {submitting ? "Đang xử lý..." : "Lưu mật khẩu"}
        </button>
      </form>

      <div className="mt-6 text-center">
        <Link to={ROUTES.LOGIN} className="text-sm font-semibold text-secondary hover:text-primary">← Quay lại trang đăng nhập</Link>
      </div>
    </AuthShell>
  );
}
