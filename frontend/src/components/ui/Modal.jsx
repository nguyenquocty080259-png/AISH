import { useEffect } from "react";
import "./Modal.css";

// open (bool), onClose (fn) — called on Esc or backdrop click, title (optional).
// Panel click is stopped from bubbling so it doesn't trigger onClose.
export default function Modal({ open, onClose, title, children, className = "" }) {
  useEffect(() => {
    if (!open) return;
    function handleKeyDown(e) {
      if (e.key === "Escape") onClose?.();
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="ui-modal__overlay" onClick={onClose}>
      <div
        className={`ui-modal ${className}`.trim()}
        role="dialog"
        aria-modal="true"
        onClick={(e) => e.stopPropagation()}
      >
        {title && <h2 className="ui-modal__title">{title}</h2>}
        {children}
      </div>
    </div>
  );
}
