import "./Alert.css";

// Hộp thông báo trong trang (khác Toast nổi ở góc màn hình).
// intent: info | success | warning | danger (alias: error) | neutral.
// Dùng cho error state của form/trang, ghi chú trạng thái kiểm duyệt, cảnh báo quota...
const INTENT_ALIAS = { error: "danger" };

export default function Alert({
  intent = "info",
  title,
  icon,
  action,
  className = "",
  children,
  ...rest
}) {
  const tone = INTENT_ALIAS[intent] ?? intent;
  const assertive = tone === "danger";

  return (
    <div
      className={`ui-alert ui-alert--${tone} ${className}`.trim()}
      role={assertive ? "alert" : "status"}
      {...rest}
    >
      {icon && (
        <span className="ui-alert__icon" aria-hidden="true">
          {icon}
        </span>
      )}
      <div className="ui-alert__content">
        {title && <p className="ui-alert__title">{title}</p>}
        {children && <div className="ui-alert__body">{children}</div>}
      </div>
      {action && <div className="ui-alert__action">{action}</div>}
    </div>
  );
}
