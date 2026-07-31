import "./Tabs.css";

// items: [{ value, label, count?, icon? }]. value + onChange do nơi gọi tự giữ state,
// component này không nhớ gì cả nên dùng chung được cho lọc trạng thái, chuyển tab hồ sơ...
// variant: line (gạch chân) | pill (viên nén trên nền surface-2).
export default function Tabs({
  items = [],
  value,
  onChange,
  variant = "line",
  ariaLabel,
  className = "",
}) {
  return (
    <div
      className={`ui-tabs ui-tabs--${variant} ${className}`.trim()}
      role="tablist"
      aria-label={ariaLabel}
    >
      {items.map((item) => {
        const active = item.value === value;
        return (
          <button
            key={item.value}
            type="button"
            role="tab"
            aria-selected={active}
            className={`ui-tabs__tab has-custom-focus ${active ? "ui-tabs__tab--active" : ""}`.trim()}
            onClick={() => onChange?.(item.value)}
          >
            {item.icon && <span className="ui-tabs__icon">{item.icon}</span>}
            <span>{item.label}</span>
            {item.count != null && <span className="ui-tabs__count">{item.count}</span>}
          </button>
        );
      })}
    </div>
  );
}
