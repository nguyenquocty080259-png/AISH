import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../hooks/useToast";
import { ROUTES } from "../constants/routes";
import { ROLES } from "../constants/roles";
import "./AppLayout.css";

// Nav chính của sidebar — chỉ hiện khi đã đăng nhập. SPACES/SHARED/FAVORITES trỏ tới
// route đã được đặt chỗ trong constants/routes.js nhưng CHƯA có trang/route thật, nên
// bấm vào sẽ rơi vào NotFoundPage cho tới khi các trang đó được xây (đúng như yêu cầu:
// "create route constants as placeholders even if the page isn't built yet").
const NAV_ITEMS = [
  { to: ROUTES.DASHBOARD, label: "Trang chủ", icon: "🏠" },
  { to: ROUTES.DOCUMENTS, label: "My Hive", icon: "📚" },
  { to: ROUTES.COMMUNITY, label: "Khám phá", icon: "🧭" },
  { to: ROUTES.SPACES, label: "Spaces", icon: "🗂️" },
  { to: ROUTES.SHARED, label: "Được chia sẻ", icon: "🔗" },
  { to: ROUTES.FAVORITES, label: "Yêu thích", icon: "⭐" },
  { to: ROUTES.AI_CHAT, label: "AI Assistant", icon: "🤖" },
  { to: ROUTES.TRASH, label: "Thùng rác", icon: "🗑️" },
];

function navLinkClassName({ isActive }) {
  return `sidebar__link${isActive ? " sidebar__link--active" : ""}`;
}

export default function AppLayout() {
  const { isAuthenticated, user, role, logout } = useAuth();
  const { showSuccess } = useToast();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    showSuccess("Đã đăng xuất.");
    navigate(ROUTES.HOME);
  };

  return (
    <div className="app-shell">
      {isAuthenticated && (
        <aside className="sidebar">
          <Link to={ROUTES.HOME} className="sidebar__brand">
            <span className="sidebar__brand-icon">🐝</span>
            <span className="sidebar__brand-name">HiveMind</span>
          </Link>

          <nav className="sidebar__nav">
            {NAV_ITEMS.map((item) => (
              <NavLink key={item.to} to={item.to} className={navLinkClassName}>
                <span className="sidebar__link-icon">{item.icon}</span>
                <span className="sidebar__link-label">{item.label}</span>
              </NavLink>
            ))}

            {role === ROLES.ADMIN && (
              <NavLink
                to={ROUTES.ADMIN}
                className={({ isActive }) =>
                  `sidebar__link sidebar__link--admin${isActive ? " sidebar__link--active" : ""}`
                }
              >
                <span className="sidebar__link-icon">🛡️</span>
                <span className="sidebar__link-label">Admin</span>
              </NavLink>
            )}
          </nav>

          <div className="sidebar__footer">
            <span className="sidebar__user">{user?.fullName}</span>
            <button className="sidebar__logout" onClick={handleLogout}>
              Đăng xuất
            </button>
          </div>
        </aside>
      )}

      <div className="app-shell__body">
        {!isAuthenticated && (
          <header className="guest-topbar">
            <Link to={ROUTES.HOME} className="guest-topbar__brand">
              🐝 HiveMind
            </Link>
            <nav className="guest-topbar__nav">
              <Link to={ROUTES.AI_CHAT}>AI Chat</Link>
              <Link to={ROUTES.LOGIN}>Đăng nhập</Link>
              <Link to={ROUTES.SIGNUP} className="guest-topbar__cta">
                Đăng ký
              </Link>
            </nav>
          </header>
        )}

        <main className="app-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
