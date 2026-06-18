import { Link } from "react-router-dom";
import { useDashboardPage } from "./hooks/useDashboardPage";
import { ROUTES } from "../../constants/routes";
import "./dashboard.css";

export default function DashboardPage() {
  const { user, handleLogout } = useDashboardPage();

  return (
    <div className="dashboard">
      <div className="dashboard__header">
        <div>
          <h1 className="dashboard__greeting">
            Chào {user?.fullName ?? "bạn"} 👋
          </h1>
          <p className="dashboard__sub">{user?.email}</p>
        </div>
        <button className="dashboard__logout" onClick={handleLogout}>
          Đăng xuất
        </button>
      </div>

      <div className="dashboard__grid">
        <Link to={ROUTES.DOCUMENTS} className="dashboard__card">
          <p className="dashboard__card-icon">📚</p>
          <h3 className="dashboard__card-title">Tài liệu</h3>
          <p className="dashboard__card-desc">
            Xem, tìm kiếm và tải lên tài liệu học tập.
          </p>
        </Link>

        <Link to={ROUTES.AI_CHAT} className="dashboard__card">
          <p className="dashboard__card-icon">🤖</p>
          <h3 className="dashboard__card-title">AI Chat</h3>
          <p className="dashboard__card-desc">
            Hỏi AI bất cứ điều gì, hoặc hỏi về một tài liệu cụ thể.
          </p>
        </Link>

        <Link to={ROUTES.PROFILE} className="dashboard__card">
          <p className="dashboard__card-icon">👤</p>
          <h3 className="dashboard__card-title">Hồ sơ cá nhân</h3>
          <p className="dashboard__card-desc">
            Cập nhật thông tin cá nhân, trường, ngành học.
          </p>
        </Link>
      </div>
    </div>
  );
}
