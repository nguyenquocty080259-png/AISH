import { Link } from "react-router-dom";
import PageHeader from "../../../components/ui/PageHeader";
import Card from "../../../components/ui/Card";
import Badge from "../../../components/ui/Badge";
import { ROUTES } from "../../../constants/routes";
import { useAdminStatsPage } from "./hooks/useAdminStatsPage";
import IngestStatusBadge from "../documents/components/IngestStatusBadge";
import AdminPagination from "../components/AdminPagination";
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

    documents,
    showDocuments,
    documentTitle,
    loadDocuments,
    documentPage,
    setDocumentPage,
    documentTotalPages,
    documentTotalElements,
  } = useAdminStatsPage();

  if (loading) {
    return (
      <div className="admin-stats-page">
        <PageHeader title="Bảng điều khiển Admin" />
        <p className="admin-stats-page__loading">
          Đang tải thống kê...
        </p>
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="admin-stats-page">
        <PageHeader title="Bảng điều khiển Admin" />
        <p className="admin-stats-page__loading">
          Không tải được thống kê.
        </p>
      </div>
    );
  }

  return (
    <div className="admin-stats-page">
      <PageHeader title="Bảng điều khiển Admin" />

      <div className="admin-stats-grid">
        {STAT_TILES.map((tile) => (
          <Card
            key={tile.key}
            clickable
            className="admin-stat-card"
            onClick={() => {
              if (tile.key === "totalUsers") {
                toggleUsers();
                return;
              }

              if (tile.key === "totalDocuments") {
                loadDocuments(null, "Danh sách tất cả tài liệu");
                return;
              }

              if (tile.key === "publicDocuments") {
                loadDocuments("PUBLIC", "Danh sách tài liệu công khai");
                return;
              }

              if (tile.key === "privateDocuments") {
                loadDocuments("PRIVATE", "Danh sách tài liệu riêng tư");
                return;
              }
            }}
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

        <Card className="admin-stat-card">
          <p className="admin-stat-card__value">{stats.docsIngested}</p>
          <p className="admin-stat-card__label">
            AI đã nạp: {stats.docsIngested} / Chưa nạp: {stats.docsNotIngested} / Không hỗ trợ: {stats.docsUnsupported}
          </p>
        </Card>
      </div>

      {/* ================= USER ================= */}

      {showUsers && (
        <Card className="admin-users-card">
          <h3>Danh sách người dùng hệ thống</h3>

          <table className="admin-users-table">
            <thead>
              <tr>
                <th style={{ width: "50%" }}>Người dùng</th>
                <th style={{ width: "15%" }}>Vai trò</th>
                <th style={{ width: "15%" }}>Trạng thái</th>
                <th style={{ width: "20%" }}>Online</th>
              </tr>
            </thead>

            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>
                    <div className="user-cell">
                      <img
                        src={
                          user.avatarUrl ||
                          "/default-avatar.png"
                        }
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
                    <span
                      className={`role-badge ${user.role.toLowerCase()}`}
                    >
                      {user.role}
                    </span>
                  </td>

                  <td>
                    <span className="status-badge">
                      {user.status}
                    </span>
                  </td>

                  <td>
                    <span
                      className={
                        user.online
                          ? "online"
                          : "offline"
                      }
                    >
                      {user.online
                        ? "🟢 Online"
                        : "⚪ Offline"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      {/* ================= DOCUMENT ================= */}

      {showDocuments && (
        <Card className="admin-users-card">
          <h3>{documentTitle}</h3>

          <table className="admin-users-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Tiêu đề</th>
                <th>Người đăng</th>
                <th>Quyền</th>
                <th>Kiểm duyệt</th>
                <th>Lưu trữ</th>
                <th>AI</th>
                <th>Ngày tạo</th>
              </tr>
            </thead>

            <tbody>
              {documents.map((doc) => (
                <tr key={doc.id}>
                  <td>{doc.id}</td>
                  <td>{doc.title}</td>

                  <td>{doc.ownerName}</td>

                  <td>{doc.visibility}</td>

                  <td>{doc.moderationStatus}</td>

                  <td>{doc.storageType}</td>

                  <td><IngestStatusBadge status={doc.ingestStatus} /></td>

                  <td>
                    {new Date(
                      doc.createdAt
                    ).toLocaleDateString("vi-VN")}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="admin-stats-doc-summary">
            <span>Tổng: {documentTotalElements} tài liệu</span>
          </div>
          <AdminPagination
            page={documentPage}
            totalPages={documentTotalPages}
            onPrev={() => setDocumentPage((currentPage) => currentPage - 1)}
            onNext={() => setDocumentPage((currentPage) => currentPage + 1)}
          />
        </Card>
      )}
    </div>
  );
}
