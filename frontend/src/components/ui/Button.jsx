import "./Button.css";
import Spinner from "./Spinner";

const VARIANT_CLASS = {
  primary: "ui-btn--primary",
  secondary: "ui-btn--secondary",
  ghost: "ui-btn--ghost",
  danger: "ui-btn--danger",
  // Bổ sung: nền tô đỏ (hành động xoá dứt khoát) và kiểu link
  "danger-solid": "ui-btn--danger-solid",
  link: "ui-btn--link",
};

const SIZE_CLASS = {
  sm: "ui-btn--sm",
  md: "ui-btn--md",
  lg: "ui-btn--lg",
};

// variant: primary | secondary | ghost | danger | danger-solid | link
// size: sm | md | lg — mặc định md, giữ đúng kích thước như bản cũ.
// loading: khoá nút + hiện spinner thay cho icon trái.
// leftIcon / rightIcon: ReactNode. iconOnly: nút vuông chỉ có icon (nhớ truyền aria-label).
// type mặc định "button" để an toàn khi đặt trong form.
export default function Button({
  variant = "primary",
  size = "md",
  loading = false,
  disabled = false,
  block = false,
  iconOnly = false,
  leftIcon = null,
  rightIcon = null,
  type = "button",
  className = "",
  children,
  ...rest
}) {
  const variantClass = VARIANT_CLASS[variant] ?? VARIANT_CLASS.primary;
  const sizeClass = SIZE_CLASS[size] ?? SIZE_CLASS.md;
  const classes = [
    "ui-btn",
    "has-custom-focus",
    variantClass,
    sizeClass,
    block ? "ui-btn--block" : "",
    iconOnly ? "ui-btn--icon-only" : "",
    loading ? "ui-btn--loading" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <button
      type={type}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      className={classes}
      {...rest}
    >
      {loading && <Spinner size={size === "lg" ? 18 : 14} />}
      {!loading && leftIcon && <span className="ui-btn__icon">{leftIcon}</span>}
      {children != null && children !== false && <span className="ui-btn__label">{children}</span>}
      {!loading && rightIcon && <span className="ui-btn__icon">{rightIcon}</span>}
    </button>
  );
}
