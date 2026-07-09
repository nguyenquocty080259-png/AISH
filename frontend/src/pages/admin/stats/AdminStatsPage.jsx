import { Link } from "react-router-dom";
import PageHeader from "../../../components/ui/PageHeader";
import Card from "../../../components/ui/Card";
import Badge from "../../../components/ui/Badge";
import { ROUTES } from "../../../constants/routes";
import { useAdminStatsPage } from "./hooks/useAdminStatsPage";
import "./admin-stats.css";

const STAT_TILES = [
  { key: "totalUsers", label: "Tổng người dùng" },
  { key: "totalDocuments", label: "Tổng tài liệu" },
  { key: "publicDocuments", label: "Tài liệu công khai" },
  { key: "privateDocuments", label: "Tài liệu riêng tư" },
  { key: "totalSubjects", label: "Tổng môn học" },
];

export default function AdminStatsPage() {
  const {
    stats,
    loading,
    users,
    showUsers,
    toggleUsers,
  } = useAdminStatsPage();

  return (
    <div className="admin-stats-page">
      <PageHeader title="Bảng điều khiển Admin" />

      {loading ? (
        <p className="admin-stats-page__loading">
          Đang tải thống kê...
        </p>
      ) : !stats ? (
        <p className="admin-stats-page__loading">
          Không tải được thống kê.
        </p>
      ) : (
        <>
          <div className="admin-stats-grid">
            {STAT_TILES.map((tile) => (
              <Card
                key={tile.key}
                clickable
                className="admin-stat-card"
                onClick={
                  tile.key === "totalUsers"
                    ? toggleUsers
                    : undefined
                }
              >
                <p className="admin-stat-card__value">
                  {stats[tile.key]}
                </p>

                <p className="admin-stat-card__label">
                  {tile.label}
                </p>
              </Card>
            ))}

            <Link
              to={ROUTES.ADMIN_APPEALS}
              className="admin-stat-card__link"
            >
              <Card
                clickable
                className="admin-stat-card admin-stat-card--highlight"
              >
                <p className="admin-stat-card__value">
                  {stats.pendingAppeals}
                </p>

                <p className="admin-stat-card__label">
                  Kháng nghị đang chờ{" "}
                  {stats.pendingAppeals > 0 && (
                    <Badge intent="warning">
                      Cần xử lý
                    </Badge>
                  )}
                </p>
              </Card>
            </Link>
          </div>

          {showUsers && (
            <Card className="admin-users-card">
              <h3>Danh sách người dùng hệ thống</h3>

              <table className="admin-users-table">
                <thead>
                  <tr>
                      <th style={{width:"50%"}}>Người dùng</th>
                      <th style={{width:"15%"}}>Vai trò</th>
                      <th style={{width:"15%"}}>Trạng thái</th>
                      <th style={{width:"20%"}}>Online</th>
                  </tr>
              </thead>

                <tbody>
                    {users.map((user) => (
                      <tr key={user.id}>
                        <td>
                            <div className="user-cell">
                                <img
                                    src={user.avatarUrl || "/default-avatar.png"}
                                    alt={user.fullName}
                                    className="admin-user-avatar"
                                />

                                <div className="user-info">

                                    <div className="user-name">

                                        {user.fullName}

                                    </div>

                                    <div className="user-email">

                                        {user.email}

                                    </div>
                                </div>
                            </div>
                        </td>
                        <td>
                            <span className={`role-badge ${user.role.toLowerCase()}`}>
                                {user.role}
                            </span>
                        </td>

                        <td>
                            <span className="status-badge">
                                {user.status}
                            </span>
                        </td>

                        <td>
                            <span className={user.online ? "online" : "offline"}>
                                {user.online ? "🟢 Online" : "⚪ Offline"}
                            </span>
                        </td>
                    </tr>
                    ))}
                  </tbody>
              </table>
            </Card>
          )}
        </>
      )}
    </div>
  );
}