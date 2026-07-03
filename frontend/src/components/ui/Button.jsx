import "./Button.css";

const VARIANT_CLASS = {
  primary: "ui-btn--primary",
  secondary: "ui-btn--secondary",
  ghost: "ui-btn--ghost",
  danger: "ui-btn--danger",
};

// variant: primary | secondary | ghost | danger. type defaults to "button" so it's safe inside forms.
export default function Button({
  variant = "primary",
  disabled = false,
  type = "button",
  className = "",
  children,
  ...rest
}) {
  const variantClass = VARIANT_CLASS[variant] ?? VARIANT_CLASS.primary;
  return (
    <button
      type={type}
      disabled={disabled}
      className={`ui-btn ${variantClass} ${className}`.trim()}
      {...rest}
    >
      {children}
    </button>
  );
}
