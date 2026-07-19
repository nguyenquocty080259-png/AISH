import { useState, useRef, useEffect } from "react";
import { useAuth } from "../../hooks/useAuth";
import NotificationBell from "../notifications/NotificationBell";
import "./top-bar.css";

// Props:
//   left?: ReactNode (nhãn trái, vd "Chế độ Quản trị"); user shell có thể null.
//   menuItems: [{ icon: ReactNode, label, onClick }]

function initials(name) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  const a = parts[0]?.[0] ?? "";
  const b = parts.length > 1 ? parts[parts.length - 1][0] : "";
  return (a + b).toUpperCase();
}

export default function TopBar({ left = null, menuItems }) {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  useEffect(() => {
    const onClick = (e) => { if (rootRef.current && !rootRef.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  return (
    <header className="sticky top-0 z-40 flex h-16 items-center justify-between gap-4 border-b border-border bg-surface px-6">
      <div className="text-sm text-secondary">{left}</div>
      <div className="flex items-center gap-4">
        <div className="topbar-bell">
          <NotificationBell />
        </div>
        <div className="relative" ref={rootRef}>
          <button type="button" onClick={() => setOpen((o) => !o)} aria-expanded={open}
            className="flex items-center gap-2 rounded-pill p-1 pr-2 transition-colors hover:bg-surface-soft">
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-surface-soft text-sm font-bold text-primary-dark">
              {initials(user?.fullName)}
            </span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-secondary"><path d="m6 9 6 6 6-6" /></svg>
          </button>

          {open && (
            <div className="absolute right-0 top-full z-50 mt-2 w-60 rounded-card border border-border bg-surface py-2 shadow-lg">
              <div className="px-4 py-2">
                <p className="text-[10px] font-semibold uppercase tracking-widest text-secondary">Tài khoản</p>
                <p className="mt-0.5 truncate font-semibold text-app">{user?.fullName ?? "Người dùng"}</p>
              </div>
              {menuItems.map((mi, i) => {
                const prev = menuItems[i - 1];
                const showDivider = i === 0 || (prev && prev.divideAfter);
                return (
                  <div key={i}>
                    {showDivider && <div className="my-1 border-t border-border" />}
                    <button type="button"
                      onClick={() => { setOpen(false); mi.onClick(); }}
                      className="flex w-full items-center gap-3 px-4 py-2.5 text-sm text-app transition-colors hover:bg-surface-soft">
                      {mi.icon}
                      {mi.label}
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
