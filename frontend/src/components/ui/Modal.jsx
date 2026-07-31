import { useEffect, useId, useRef } from "react";
import "./Modal.css";

const SIZE_CLASS = {
  sm: "ui-modal--sm",
  md: "",
  lg: "ui-modal--lg",
  xl: "ui-modal--xl",
};

// open (bool), onClose (fn) — gọi khi bấm Esc hoặc bấm nền mờ, title (tuỳ chọn).
// Click trong panel được chặn nổi bọt nên không đóng nhầm.
// Thêm (không phá API cũ): size sm|md|lg|xl, description, footer, showClose,
// khoá cuộn trang nền và trả focus về nơi vừa bấm khi đóng.
export default function Modal({
  open,
  onClose,
  title,
  description,
  footer,
  size = "md",
  showClose = true,
  children,
  className = "",
}) {
  const panelRef = useRef(null);
  const lastFocusedRef = useRef(null);
  const titleId = useId();
  const descId = useId();

  useEffect(() => {
    if (!open) return undefined;

    lastFocusedRef.current = document.activeElement;

    function handleKeyDown(e) {
      if (e.key === "Escape") onClose?.();
    }
    window.addEventListener("keydown", handleKeyDown);

    // Khoá cuộn trang nền khi modal mở, trả lại đúng giá trị cũ khi đóng.
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    // Đưa focus vào panel để người dùng bàn phím không bị kẹt ngoài modal.
    panelRef.current?.focus();

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = previousOverflow;
      lastFocusedRef.current?.focus?.();
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="ui-modal__overlay" onClick={onClose}>
      <div
        ref={panelRef}
        tabIndex={-1}
        className={`ui-modal ${SIZE_CLASS[size] ?? ""} ${className}`.trim()}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-describedby={description ? descId : undefined}
        onClick={(e) => e.stopPropagation()}
      >
        {(title || showClose) && (
          <div className="ui-modal__header">
            <div className="ui-modal__header-text">
              {title && (
                <h2 className="ui-modal__title" id={titleId}>
                  {title}
                </h2>
              )}
              {description && (
                <p className="ui-modal__description" id={descId}>
                  {description}
                </p>
              )}
            </div>
            {showClose && onClose && (
              <button
                type="button"
                className="ui-modal__close has-custom-focus"
                onClick={onClose}
                aria-label="Close"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                  <path d="M18 6 6 18M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>
        )}

        <div className="ui-modal__body">{children}</div>

        {footer && <div className="ui-modal__footer">{footer}</div>}
      </div>
    </div>
  );
}
