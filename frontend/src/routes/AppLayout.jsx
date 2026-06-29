import { Link, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../hooks/useToast";
import { ROUTES } from "../constants/routes";
import "./AppLayout.css";

// Layout dùng chung cho toàn app: thanh điều hướng trên cùng + <Outlet/>.
// Đặt trong routes/ vì đây là thành phần thuộc "Quản lý routing", không phải
// 1 feature cụ thể, nên không vi phạm rule "không tạo components/common".
export default function AppLayout() {
  const { isAuthenticated, user, logout } = useAuth();
  const { showSuccess } = useToast();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    showSuccess("Đã đăng xuất.");
    navigate(ROUTES.HOME);
  };

  return (
    <div className="app-layout">
      <header className="app-header">
        <Link to={ROUTES.HOME} className="app-header__brand">
          AISH
        </Link>

        <nav className="app-header__nav">
          {isAuthenticated ? (
            <>
              <Link to={ROUTES.DOCUMENTS}>Tài liệu</Link>
              <Link to={ROUTES.AI_CHAT}>AI Chat</Link>
              <Link to={ROUTES.DASHBOARD}>Dashboard</Link>
              <Link to={ROUTES.PROFILE}>Hồ sơ</Link>
              <span className="app-header__user">{user?.fullName}</span>
              <button className="app-header__logout" onClick={handleLogout}>
                Đăng xuất
              </button>
            </>
          ) : (
            <>
              <Link to={ROUTES.AI_CHAT}>AI Chat</Link>
              <Link to={ROUTES.LOGIN}>Đăng nhập</Link>
              <Link to={ROUTES.SIGNUP} className="app-header__cta">
                Đăng ký
              </Link>
            </>
          )}
        </nav>
      </header>

      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
