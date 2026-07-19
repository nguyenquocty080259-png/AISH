// src/pages/auth/ForgotPasswordPage.jsx

import { useState } from "react";
import { useNavigate } from "react-router-dom";

import * as authApi from "../../api/authApi";
import { ROUTES } from "../../constants/routes";

export default function ForgotPasswordPage() {

  const [email, setEmail] = useState("");

  const navigate = useNavigate();

  const handleSubmit = async (e) => {

    e.preventDefault();

    try {

      await authApi.forgotPassword({
        email,
      });

      navigate(
        ROUTES.VERIFY_OTP,
        {
          state: {
            email,
            mode: "forgot-password"
          }
        }
      );
    } catch (err) {

      console.error(err);

    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">

        <h1 className="auth-card__title">
          Quên mật khẩu
        </h1>

        <p className="auth-card__subtitle">
          Nhập email để nhận mã OTP.
        </p>

        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >
          <div className="auth-field">
            <label>Email</label>

            <input
              type="email"
              required
              value={email}
              onChange={(e) =>
                setEmail(e.target.value)
              }
            />
          </div>  

          <button
            type="submit"
            className="auth-submit"
          >
            Gửi OTP
          </button>

        </form>

      </div>
    </div>
  );
}
