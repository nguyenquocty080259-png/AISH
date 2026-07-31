import "./Card.css";

const PADDING_CLASS = {
  none: "ui-card--pad-none",
  sm: "ui-card--pad-sm",
  md: "",
  lg: "ui-card--pad-lg",
};

// clickable: thêm hiệu ứng hover (viền + bóng + nhấc nhẹ) cho card đóng vai trò link/button.
// padding: none | sm | md | lg — md giữ đúng padding cũ.
// elevated: có bóng sẵn ở trạng thái tĩnh. flush: bỏ padding để tự dựng Header/Body/Footer.
export default function Card({
  clickable = false,
  elevated = false,
  padding = "md",
  as: Tag = "div",
  className = "",
  children,
  ...rest
}) {
  const classes = [
    "ui-card",
    clickable ? "ui-card--clickable" : "",
    elevated ? "ui-card--elevated" : "",
    PADDING_CLASS[padding] ?? "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <Tag className={classes} {...rest}>
      {children}
    </Tag>
  );
}

// Các slot dưới đây là tuỳ chọn — card cũ vẫn truyền children thẳng như trước.
Card.Header = function CardHeader({ title, subtitle, actions, className = "", children }) {
  return (
    <div className={`ui-card__header ${className}`.trim()}>
      {(title || subtitle) && (
        <div className="ui-card__header-text">
          {title && <h3 className="ui-card__title">{title}</h3>}
          {subtitle && <p className="ui-card__subtitle">{subtitle}</p>}
        </div>
      )}
      {children}
      {actions && <div className="ui-card__header-actions">{actions}</div>}
    </div>
  );
};

Card.Body = function CardBody({ className = "", children }) {
  return <div className={`ui-card__body ${className}`.trim()}>{children}</div>;
};

Card.Footer = function CardFooter({ className = "", children }) {
  return <div className={`ui-card__footer ${className}`.trim()}>{children}</div>;
};
