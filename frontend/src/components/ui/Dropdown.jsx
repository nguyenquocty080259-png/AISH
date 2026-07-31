import { useEffect, useRef, useState } from "react";
import "./Dropdown.css";

// Menu bật/tắt quanh một nút kích hoạt. Tự đóng khi bấm ra ngoài hoặc bấm Esc.
// trigger: hàm nhận ({ open, toggle, ref? }) hoặc ReactNode — nếu là node thì component
//   tự bọc trong <button>.
// items: [{ label, icon?, onClick?, danger?, disabled?, divideBefore? }]; muốn tự do hơn
//   thì truyền children thay cho items.
// align: right | left (mặc định right, mép phải trùng mép phải nút).
export default function Dropdown({
  trigger,
  items,
  align = "right",
  ariaLabel,
  className = "",
  menuClassName = "",
  children,
}) {
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;

    function onPointerDown(e) {
      if (rootRef.current && !rootRef.current.contains(e.target)) setOpen(false);
    }
    function onKeyDown(e) {
      if (e.key === "Escape") setOpen(false);
    }

    document.addEventListener("mousedown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("mousedown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [open]);

  const toggle = () => setOpen((o) => !o);
  const close = () => setOpen(false);

  return (
    <div className={`ui-dropdown ${className}`.trim()} ref={rootRef}>
      {typeof trigger === "function" ? (
        trigger({ open, toggle, close })
      ) : (
        <button
          type="button"
          className="ui-dropdown__trigger has-custom-focus"
          onClick={toggle}
          aria-expanded={open}
          aria-haspopup="menu"
          aria-label={ariaLabel}
        >
          {trigger}
        </button>
      )}

      {open && (
        <div
          className={`ui-dropdown__menu ui-dropdown__menu--${align} ${menuClassName}`.trim()}
          role="menu"
        >
          {items?.map((item, i) => (
            <div key={i}>
              {item.divideBefore && <div className="ui-dropdown__divider" role="separator" />}
              <button
                type="button"
                role="menuitem"
                disabled={item.disabled}
                className={`ui-dropdown__item has-custom-focus ${
                  item.danger ? "ui-dropdown__item--danger" : ""
                }`.trim()}
                onClick={() => {
                  close();
                  item.onClick?.();
                }}
              >
                {item.icon && <span className="ui-dropdown__item-icon">{item.icon}</span>}
                <span>{item.label}</span>
              </button>
            </div>
          ))}
          {typeof children === "function" ? children({ close }) : children}
        </div>
      )}
    </div>
  );
}
