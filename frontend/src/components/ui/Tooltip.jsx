import "./Tooltip.css";

// Tooltip thuần CSS: bọc phần tử con, hiện label khi hover hoặc focus bàn phím.
// label cũng được gắn vào aria-label của lớp bọc để screen reader đọc được.
// placement: top | bottom.
export default function Tooltip({ label, placement = "top", className = "", children }) {
  if (!label) return children;

  return (
    <span className={`ui-tooltip ui-tooltip--${placement} ${className}`.trim()} tabIndex={-1}>
      {children}
      <span className="ui-tooltip__bubble" role="tooltip">
        {label}
      </span>
    </span>
  );
}
