import { useAuth } from "../../context/AuthContext";
import "./DashboardPage.css";

export default function DashboardPage() {
  const { user, logout } = useAuth();

  return (
    <div className="dashboard">
      <aside className="dashboard__sidebar">
        <div className="dashboard__logo">
          <span className="dashboard__logo-icon">✦</span>
          <span className="dashboard__logo-text">AISH</span>
        </div>

        <nav className="dashboard__nav">
          <a href="#" className="dashboard__nav-item dashboard__nav-item--active">
            🏠 Tổng quan
          </a>
          <a href="#" className="dashboard__nav-item">
            📄 Tài liệu
          </a>
          <a href="#" className="dashboard__nav-item">
            💬 Chat AI
          </a>
          <a href="#" className="dashboard__nav-item">
            ⚙️ Cài đặt
          </a>
        </nav>

        <button className="dashboard__logout" onClick={logout}>
          Đăng xuất
        </button>
      </aside>

      <main className="dashboard__main">
        <header className="dashboard__header">
          <div>
            <h1 className="dashboard__title">Xin chào 👋</h1>
            <p className="dashboard__subtitle">
              {user?.fullName || user?.email || "Học viên"}
            </p>
          </div>
        </header>

        <div className="dashboard__content">
          <div className="dashboard__card">
            <h3>🚀 Dashboard đang được xây dựng</h3>
            <p>Các tính năng sẽ sớm được cập nhật.</p>
          </div>
        </div>
      </main>
    </div>
  );
}