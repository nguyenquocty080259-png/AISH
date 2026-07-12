import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import { useAuth } from "../../hooks/useAuth";
import { useToast } from "../../hooks/useToast";
import NotificationBell from "../../components/notifications/NotificationBell";
import "./admin-layout.css";

const ADMIN_NAV_ITEMS = [
  { to: ROUTES.ADMIN_REPORTS, label: "Báo cáo", icon: "⚑" },
  { to: ROUTES.ADMIN_STATS, label: "Bảng điều khiển", icon: "▦" },
  { to: ROUTES.ADMIN_APPEALS, label: "Kháng nghị", icon: "⚑" },
  { to: ROUTES.ADMIN_DOCUMENTS, label: "Tài liệu", icon: "▤" },
  { to: ROUTES.ADMIN_SUBJECTS, label: "Môn học", icon: "◫" },
  { to: ROUTES.ADMIN_USERS, label: "Người dùng", icon: "♙" },
];

function adminNavClassName({ isActive }) {
  return `admin-sidebar__link${isActive ? " admin-sidebar__link--active" : ""}`;
}

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const { showSuccess } = useToast();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    showSuccess("Đã đăng xuất.");
    navigate(ROUTES.HOME);
  };

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <Link to={ROUTES.ADMIN} className="admin-sidebar__brand">
          <span className="admin-sidebar__brand-icon">🛡️</span>
          <span>HiveMind Admin</span>
        </Link>

        <nav className="admin-sidebar__nav" aria-label="Điều hướng quản trị">
          {ADMIN_NAV_ITEMS.map((item) => (
            <NavLink key={item.to} to={item.to} className={adminNavClassName}>
              <span className="admin-sidebar__link-icon" aria-hidden="true">
                {item.icon}
              </span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="admin-sidebar__footer">
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8 }}>
            <span className="admin-sidebar__user">{user?.fullName}</span>
            <NotificationBell />
          </div>
          <button
            type="button"
            className="admin-sidebar__user-view"
            onClick={() => navigate(ROUTES.DASHBOARD)}
          >
            Xem như User
          </button>
          <button
            type="button"
            className="admin-sidebar__logout"
            onClick={handleLogout}
          >
            Đăng xuất
          </button>
        </div>
      </aside>

      <main className="admin-layout__content">
        <Outlet />
      </main>
    </div>
  );
}
