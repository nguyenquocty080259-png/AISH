import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Input } from "../ui/Field";
import "./auth.css";

const EyeOff = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 10 8 10 8a13.2 13.2 0 0 1-1.67 2.68"/><path d="M6.61 6.61A13.5 13.5 0 0 0 2 12s3 8 10 8a9.7 9.7 0 0 0 5.39-1.61"/><line x1="2" y1="2" x2="22" y2="22"/></svg>
);
const Eye = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M2 12s3-8 10-8 10 8 10 8-3 8-10 8-10-8-10-8Z"/><circle cx="12" cy="12" r="3"/></svg>
);

// Input mật khẩu có nút ẩn/hiện (thuần FE). Nếu không truyền label thì caller tự render label.
export default function PasswordField({ id, label, value, onChange, placeholder, helper, minLength, required = true }) {
  const { t } = useTranslation();
  const [show, setShow] = useState(false);

  return (
    <Input
      id={id}
      label={label}
      hint={helper}
      type={show ? "text" : "password"}
      required={required}
      minLength={minLength}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      className="auth-password__input"
      rightSlot={
        <button
          type="button"
          onClick={() => setShow((s) => !s)}
          aria-label={show ? t("auth.password.hide") : t("auth.password.show")}
          className="auth-password__toggle has-custom-focus"
        >
          {show ? <EyeOff /> : <Eye />}
        </button>
      }
    />
  );
}
