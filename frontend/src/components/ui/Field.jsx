import { useId } from "react";
import "./Field.css";

// Khung dùng chung cho Input / Textarea / Select: label, dấu bắt buộc, helper text,
// thông báo lỗi (aria-invalid + aria-describedby để screen reader đọc đúng).
// Nhận sẵn id từ ngoài nếu nơi gọi đã tự quản lý id.
export function Field({ id, label, hint, error, required, htmlFor, className = "", children }) {
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined;

  return (
    <div className={`ui-field ${error ? "ui-field--invalid" : ""} ${className}`.trim()}>
      {label && (
        <label className="ui-field__label" htmlFor={htmlFor ?? id}>
          {label}
          {required && (
            <span className="ui-field__required" aria-hidden="true">
              *
            </span>
          )}
        </label>
      )}
      {typeof children === "function" ? children({ describedBy }) : children}
      {error ? (
        <p className="ui-field__error" id={`${id}-error`} role="alert">
          {error}
        </p>
      ) : (
        hint && (
          <p className="ui-field__hint" id={`${id}-hint`}>
            {hint}
          </p>
        )
      )}
    </div>
  );
}

// Ô nhập một dòng. leftIcon/rightSlot để gắn icon tìm kiếm, nút xoá, đơn vị...
export function Input({
  id,
  label,
  hint,
  error,
  required,
  leftIcon,
  rightSlot,
  className = "",
  fieldClassName = "",
  ...rest
}) {
  const autoId = useId();
  const inputId = id ?? autoId;

  return (
    <Field
      id={inputId}
      label={label}
      hint={hint}
      error={error}
      required={required}
      className={fieldClassName}
    >
      {({ describedBy }) => (
        <div className={`ui-input-wrap ${leftIcon ? "ui-input-wrap--has-left" : ""}`.trim()}>
          {leftIcon && (
            <span className="ui-input__adornment" aria-hidden="true">
              {leftIcon}
            </span>
          )}
          <input
            id={inputId}
            className={`ui-input has-custom-focus ${className}`.trim()}
            aria-invalid={error ? true : undefined}
            aria-describedby={describedBy}
            required={required}
            {...rest}
          />
          {rightSlot && <span className="ui-input__right">{rightSlot}</span>}
        </div>
      )}
    </Field>
  );
}

export function Textarea({
  id,
  label,
  hint,
  error,
  required,
  rows = 4,
  className = "",
  fieldClassName = "",
  ...rest
}) {
  const autoId = useId();
  const inputId = id ?? autoId;

  return (
    <Field
      id={inputId}
      label={label}
      hint={hint}
      error={error}
      required={required}
      className={fieldClassName}
    >
      {({ describedBy }) => (
        <textarea
          id={inputId}
          rows={rows}
          className={`ui-input ui-textarea has-custom-focus ${className}`.trim()}
          aria-invalid={error ? true : undefined}
          aria-describedby={describedBy}
          required={required}
          {...rest}
        />
      )}
    </Field>
  );
}

// Select native (giữ hành vi chọn của hệ điều hành, tốt cho mobile), chỉ vẽ lại mũi tên.
export function Select({
  id,
  label,
  hint,
  error,
  required,
  className = "",
  fieldClassName = "",
  children,
  ...rest
}) {
  const autoId = useId();
  const inputId = id ?? autoId;

  return (
    <Field
      id={inputId}
      label={label}
      hint={hint}
      error={error}
      required={required}
      className={fieldClassName}
    >
      {({ describedBy }) => (
        <div className="ui-select-wrap">
          <select
            id={inputId}
            className={`ui-input ui-select has-custom-focus ${className}`.trim()}
            aria-invalid={error ? true : undefined}
            aria-describedby={describedBy}
            required={required}
            {...rest}
          >
            {children}
          </select>
          <svg
            className="ui-select__chevron"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="m6 9 6 6 6-6" />
          </svg>
        </div>
      )}
    </Field>
  );
}

export default Input;
