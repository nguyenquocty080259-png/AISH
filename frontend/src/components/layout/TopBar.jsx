import { useState, useRef, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../hooks/useAuth";
import NotificationBell from "../notifications/NotificationBell";
import LanguageSwitcher from "../common/LanguageSwitcher";
import Avatar from "../ui/Avatar";
import "./top-bar.css";

// Props:
//   left?: ReactNode (nhãn trái, vd "Chế độ Quản trị"); user shell có thể null.
//   menuItems: [{ icon: ReactNode, label, onClick, divideAfter? }]
//   onMenuClick?: mở ngăn kéo sidebar — nút hamburger chỉ hiện dưới 768px.

export default function TopBar({ left = null, menuItems, onMenuClick }) {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  useEffect(() => {
    const onClick = (e) => { if (rootRef.current && !rootRef.current.contains(e.target)) setOpen(false); };
    const onKeyDown = (e) => { if (e.key === "Escape") setOpen(false); };
    document.addEventListener("mousedown", onClick);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("mousedown", onClick);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, []);

  return (
    <header className="top-bar">
      <div className="top-bar__left">
        {onMenuClick && (
          <button
            type="button"
            className="top-bar__menu-btn has-custom-focus"
            onClick={onMenuClick}
            aria-label={t("common.openMenu")}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <path d="M3 6h18M3 12h18M3 18h18" />
            </svg>
          </button>
        )}
        {left && <span className="top-bar__label">{left}</span>}
      </div>

      <div className="top-bar__right">
        <LanguageSwitcher />
        <div className="topbar-bell">
          <NotificationBell />
        </div>

        <div className="top-bar__user" ref={rootRef}>
          <button
            type="button"
            onClick={() => setOpen((o) => !o)}
            aria-expanded={open}
            aria-haspopup="menu"
            aria-label={t("common.account")}
            className="top-bar__user-btn has-custom-focus"
          >
            <Avatar name={user?.fullName} size="sm" />
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="m6 9 6 6 6-6" />
            </svg>
          </button>

          {open && (
            <div className="top-bar__menu" role="menu">
              <div className="top-bar__menu-head">
                <p className="top-bar__menu-caption">{t("common.account")}</p>
                <p className="top-bar__menu-name">{user?.fullName ?? t("common.user")}</p>
              </div>
              {menuItems.map((mi, i) => {
                const prev = menuItems[i - 1];
                const showDivider = i === 0 || (prev && prev.divideAfter);
                return (
                  <div key={i}>
                    {showDivider && <div className="top-bar__menu-divider" role="separator" />}
                    <button
                      type="button"
                      role="menuitem"
                      onClick={() => { setOpen(false); mi.onClick(); }}
                      className="top-bar__menu-item has-custom-focus"
                    >
                      <span className="top-bar__menu-icon">{mi.icon}</span>
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
