import "./Badge.css";

// Gộp các tên intent cùng nghĩa về 1 lớp CSS, để nơi gọi cũ ("error") và
// tên semantic mới ("danger") dùng lẫn nhau đều ra cùng màu.
const INTENT_ALIAS = {
  error: "danger",
  accent: "brand",
  primary: "brand",
};

// intent: neutral | success | warning | danger (alias: error) | info | brand
//   Dùng cho badge lưu trữ (LOCAL/CLOUD), trạng thái kiểm duyệt
//   (ADMIN_PENDING = warning, APPROVED = success, REJECTED = danger), nhãn, tag...
// size: sm | md — md là mặc định cũ.
// dot: hiện chấm tròn cùng màu ở đầu badge (trạng thái đang chạy/chờ).
// outline: nền trong suốt, chỉ giữ viền + chữ.
export default function Badge({
  intent = "neutral",
  size = "md",
  dot = false,
  outline = false,
  className = "",
  children,
  ...rest
}) {
  const tone = INTENT_ALIAS[intent] ?? intent;
  const classes = [
    "ui-badge",
    `ui-badge--${tone}`,
    size === "sm" ? "ui-badge--sm" : "",
    outline ? "ui-badge--outline" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <span className={classes} {...rest}>
      {dot && <span className="ui-badge__dot" aria-hidden="true" />}
      {children}
    </span>
  );
}
