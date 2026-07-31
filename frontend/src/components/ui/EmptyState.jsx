import Button from "./Button";
import "./EmptyState.css";

// icon: emoji/node tuỳ chọn. message: bắt buộc. actionLabel + onAction: nút CTA tuỳ chọn.
// Thêm (không phá API cũ): title cho dòng tiêu đề đậm phía trên message,
// action nhận node tự do, tone "danger" để dùng luôn làm error state.
export default function EmptyState({
  icon,
  title,
  message,
  actionLabel,
  onAction,
  action,
  tone = "neutral",
  className = "",
}) {
  return (
    <div className={`ui-empty-state ui-empty-state--${tone} ${className}`.trim()}>
      {icon && (
        <div className="ui-empty-state__icon" aria-hidden="true">
          {icon}
        </div>
      )}
      {title && <p className="ui-empty-state__title">{title}</p>}
      {message && <p className="ui-empty-state__message">{message}</p>}
      {action}
      {actionLabel && onAction && (
        <Button variant="secondary" onClick={onAction} className="ui-empty-state__action">
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
