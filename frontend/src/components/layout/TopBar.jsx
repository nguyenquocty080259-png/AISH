import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useToast } from "../../hooks/useToast";
import { ROUTES } from "../../constants/routes";
import { ROLES } from "../../constants/roles";
import NotificationBell from "../notifications/NotificationBell";
import "./top-bar.css";

function initials(name) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  const a = parts[0]?.[0] ?? "";
  const b = parts.length > 1 ? parts[parts.length - 1][0] : "";
  return (a + b).toUpperCase();
}

export default function TopBar() {
  const { user, role, logout } = useAuth();
  const { showSuccess } = useToast();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  useEffect(() => {
    const onClick = (e) => { if (rootRef.current && !rootRef.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  const handleLogout = async () => {
    setOpen(false);
    await logout();
    showSuccess("Đã đăng xuất.");
    navigate(ROUTES.HOME);
  };
  const go = (route) => { setOpen(false); navigate(route); };

  return (
    <header className="sticky top-0 z-40 flex h-16 items-center justify-end gap-4 border-b border-border bg-surface px-6">
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
            <div className="my-1 border-t border-border" />
            <button type="button" onClick={() => go(ROUTES.PROFILE)}
              className="flex w-full items-center gap-3 px-4 py-2.5 text-sm text-app transition-colors hover:bg-surface-soft">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="8" r="4" /><path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1" /></svg>
              Trang cá nhân
            </button>
            {role === ROLES.ADMIN && (
              <button type="button" onClick={() => go(ROUTES.ADMIN)}
                className="flex w-full items-center gap-3 px-4 py-2.5 text-sm text-app transition-colors hover:bg-surface-soft">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 3l7 4v5c0 4-3 7-7 9-4-2-7-5-7-9V7l7-4Z" /></svg>
                Về chế độ Admin
              </button>
            )}
            <div className="my-1 border-t border-border" />
            <button type="button" onClick={handleLogout}
              className="flex w-full items-center gap-3 px-4 py-2.5 text-sm text-app transition-colors hover:bg-surface-soft">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="M16 17l5-5-5-5" /><path d="M21 12H9" /></svg>
              Đăng xuất
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
