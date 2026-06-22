import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import * as authApi from "../../api/authApi";
import { ROUTES } from "../../constants/routes";

import "./auth.css";

export default function ResetPasswordPage() {

  const navigate = useNavigate();

  const { state } = useLocation();

  const email = state?.email ?? "";

  const [password, setPassword] = useState("");

  const [confirmPassword, setConfirmPassword] = useState("");

  const [submitting, setSubmitting] = useState(false);

  const [error, setError] = useState("");

  const handleSubmit = async (e) => {

    e.preventDefault();

    setError("");

    if (!email) {

      setError(
        "Không tìm thấy thông tin email."
      );

      return;
    }

    if (password.length < 8) {

      setError(
        "Mật khẩu phải có ít nhất 8 ký tự."
      );

      return;
    }

    if (password !== confirmPassword) {

      setError(
        "Mật khẩu xác nhận không khớp."
      );

      return;
    }

    try {

      setSubmitting(true);

      await authApi.resetPassword({
        email,
        password
      });

      alert(
        "Đổi mật khẩu thành công."
      );

      navigate(
        ROUTES.LOGIN
      );

    } catch (err) {

      setError(
        err?.response?.data?.message ??
        "Đổi mật khẩu thất bại."
      );

    } finally {

      setSubmitting(false);

    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">

        <h1 className="auth-card__title">
          Đặt lại mật khẩu
        </h1>

        <p className="auth-card__subtitle">
          Tạo mật khẩu mới cho tài khoản.
        </p>

        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >

          <div className="auth-field">
            <label>Mật khẩu mới</label>

            <input
              type="password"
              required
              value={password}
              onChange={(e) =>
                setPassword(
                  e.target.value
                )
              }
            />
          </div>

          <div className="auth-field">
            <label>
              Xác nhận mật khẩu
            </label>

            <input
              type="password"
              required
              value={confirmPassword}
              onChange={(e) =>
                setConfirmPassword(
                  e.target.value
                )
              }
            />
          </div>

          {error && (
            <p
              style={{
                color: "red"
              }}
            >
              {error}
            </p>
          )}

          <button
            type="submit"
            className="auth-submit"
            disabled={submitting}
          >
            {
              submitting
                ? "Đang xử lý..."
                : "Lưu mật khẩu"
            }
          </button>

        </form>

      </div>
    </div>
  );
}