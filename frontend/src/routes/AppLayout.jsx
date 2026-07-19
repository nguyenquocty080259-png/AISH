import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../hooks/useToast";
import { ROUTES } from "../constants/routes";
import { ROLES } from "../constants/roles";
import AIHiveMindWidget from "../components/ai-widget/AIHiveMindWidget";
import NotificationBell from "../components/notifications/NotificationBell";
import GuestNavbar from "../components/guest/GuestNavbar";
import GuestFooter from "../components/guest/GuestFooter";
import { AiWidgetProvider } from "../context/AiWidgetContext";
import "./AppLayout.css";

// Nav chính của sidebar — chỉ hiện khi đã đăng nhập.
// "Shared" đã bị bỏ hẳn khỏi nav: backend isSharedTo() luôn trả false (V2, chưa có dữ liệu share thật).
const NAV_ITEMS = [
  { to: ROUTES.MY_REPORTS, label: "Báo cáo của tôi", icon: "⚑" },
  { to: ROUTES.DASHBOARD, label: "Trang chủ", icon: "🏠" },
  { to: ROUTES.DOCUMENTS, label: "My Hive", icon: "📚" },
  { to: ROUTES.COMMUNITY, label: "Khám phá", icon: "🧭" },
  { to: ROUTES.SPACES, label: "Spaces", icon: "🗂️" },
  { to: ROUTES.FAVORITES, label: "Yêu thích", icon: "⭐" },
  { to: ROUTES.AI_CHAT, label: "AI HiveMind", icon: "🤖" },
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
    <AiWidgetProvider>
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
            </nav>

            <div className="sidebar__footer">
              <div className="sidebar__identity">
                <span className="sidebar__user">{user?.fullName}</span>
                <NotificationBell />
                {role === ROLES.ADMIN && (
                  <Link to={ROUTES.ADMIN} className="sidebar__admin-return">
                    Về chế độ Admin
                  </Link>
                )}
              </div>
              <button className="sidebar__logout" onClick={handleLogout}>
                Đăng xuất
              </button>
            </div>
          </aside>
        )}

        <div className="app-shell__body">
          {!isAuthenticated && <GuestNavbar />}

          <main className="app-main">
            <Outlet />
          </main>

          {!isAuthenticated && <GuestFooter />}
        </div>
      </div>
      {isAuthenticated && <AIHiveMindWidget />}
    </AiWidgetProvider>
  );
}
