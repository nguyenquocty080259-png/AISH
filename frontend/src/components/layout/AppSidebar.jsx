import { useEffect } from "react";
import { NavLink, Link } from "react-router-dom";
import "./app-sidebar.css";

// Props (giữ nguyên như trước):
//   brand: { logoSrc, name, caption, homeTo }
//   navGroups: [{ label?: string, items: [{ to, label, icon: ReactNode, badge?: number }] }]
//   bottomAction?: { label, to, icon: ReactNode }
// Nếu chỉ 1 group không label → render phẳng (không caption group, không divider).
// Nhiều group hoặc group có label → render label in hoa + divider giữa các group.
//
// Thêm cho responsive:
//   open / onClose — dưới 768px sidebar thành ngăn kéo trượt từ trái, TopBar mở nó bằng
//   nút hamburger. Từ 768px trở lên hai prop này không có tác dụng.

export default function AppSidebar({ brand, navGroups, bottomAction, open = false, onClose }) {
  const showGroupLabels = navGroups.length > 1 || navGroups.some((g) => g.label);

  // Ngăn kéo đang mở: Esc để đóng + khoá cuộn nền cho khỏi cuộn trang phía sau.
  useEffect(() => {
    if (!open) return undefined;

    function onKeyDown(e) {
      if (e.key === "Escape") onClose?.();
    }
    document.addEventListener("keydown", onKeyDown);

    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    return () => {
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = previousOverflow;
    };
  }, [open, onClose]);

  return (
    <>
      {open && <div className="app-sidebar__scrim" onClick={onClose} aria-hidden="true" />}

      <aside className={`app-sidebar ${open ? "app-sidebar--open" : ""}`.trim()}>
        <div className="app-sidebar__head">
          <Link to={brand.homeTo} className="app-sidebar__brand" onClick={onClose}>
            <img src={brand.logoSrc} alt="" className="app-sidebar__logo" />
            <span className="app-sidebar__brand-text">
              <span className="app-sidebar__brand-name">{brand.name}</span>
              {brand.caption && (
                <span className="app-sidebar__brand-caption">{brand.caption}</span>
              )}
            </span>
          </Link>
        </div>

        <nav className="app-sidebar__nav">
          {navGroups.map((group, gi) => (
            <div key={gi} className="app-sidebar__group">
              {gi > 0 && <div className="app-sidebar__divider" />}
              {showGroupLabels && group.label && (
                <span className="app-sidebar__group-label">{group.label}</span>
              )}
              {group.items.map((it) => {
                const hasBadge = it.badge != null && it.badge > 0;
                const badgeText = it.badge > 99 ? "99+" : it.badge;
                return (
                  <NavLink
                    key={it.to}
                    to={it.to}
                    onClick={onClose}
                    className={({ isActive }) =>
                      `app-sidebar__link has-custom-focus ${
                        isActive ? "app-sidebar__link--active" : ""
                      }`.trim()
                    }
                  >
                    <span className="app-sidebar__link-icon">
                      {it.icon}
                      {hasBadge && <span className="app-sidebar__dot" aria-hidden="true" />}
                    </span>
                    <span className="app-sidebar__link-label">{it.label}</span>
                    {hasBadge && <span className="app-sidebar__badge">{badgeText}</span>}
                  </NavLink>
                );
              })}
            </div>
          ))}
        </nav>

        {bottomAction && (
          <Link to={bottomAction.to} className="app-sidebar__cta has-custom-focus" onClick={onClose}>
            {bottomAction.icon}
            <span className="app-sidebar__link-label">{bottomAction.label}</span>
          </Link>
        )}
      </aside>
    </>
  );
}
