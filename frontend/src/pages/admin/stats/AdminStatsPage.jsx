import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PageHeader from "../../../components/ui/PageHeader";
import Card from "../../../components/ui/Card";
import Badge from "../../../components/ui/Badge";
import { ROUTES } from "../../../constants/routes";
import { useAdminStatsPage } from "./hooks/useAdminStatsPage";
import IngestStatusBadge from "../documents/components/IngestStatusBadge";
import AdminPagination from "../components/AdminPagination";
import AdminAiUsageCard from "../../../components/admin/AdminAiUsageCard";
import AiUsageReport from "../../../components/admin/AiUsageReport";
import "./admin-stats.css";

const STAT_TILES = [
  { key: "totalUsers", labelKey: "admin.stats.totalUsers" },
  { key: "totalDocuments", labelKey: "admin.stats.totalDocuments" },
  { key: "publicDocuments", labelKey: "admin.stats.publicDocuments" },
  { key: "privateDocuments", labelKey: "admin.stats.privateDocuments" },
  { key: "totalSubjects", labelKey: "admin.stats.totalSubjects" },
];

export default function AdminStatsPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
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
        <PageHeader title={t("admin.stats.title")} />
        <p className="admin-stats-page__loading">
          {t("admin.stats.loading")}
        </p>
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="admin-stats-page">
        <PageHeader title={t("admin.stats.title")} />
        <p className="admin-stats-page__loading">
          {t("admin.stats.loadError")}
        </p>
      </div>
    );
  }

  return (
    <div className="admin-stats-page">
      <PageHeader title={t("admin.stats.title")} />

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
                loadDocuments(null, t("admin.stats.allDocsTitle"));
                return;
              }

              if (tile.key === "publicDocuments") {
                loadDocuments("PUBLIC", t("admin.stats.publicDocsTitle"));
                return;
              }

              if (tile.key === "privateDocuments") {
                loadDocuments("PRIVATE", t("admin.stats.privateDocsTitle"));
                return;
              }
            }}
          >
            <p className="admin-stat-card__value">
              {stats[tile.key]}
            </p>

            <p className="admin-stat-card__label">
              {t(tile.labelKey)}
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
              {t("admin.stats.pendingAppeals")}{" "}
              {stats.pendingAppeals > 0 && (
                <Badge intent="warning">
                  {t("admin.stats.needAction")}
                </Badge>
              )}
            </p>
          </Card>
        </Link>

        <Card className="admin-stat-card">
          <p className="admin-stat-card__value">{stats.docsIngested}</p>
          <p className="admin-stat-card__label">
            {t("admin.stats.aiIngestSummary", { ingested: stats.docsIngested, notIngested: stats.docsNotIngested, unsupported: stats.docsUnsupported })}
          </p>
        </Card>
      </div>

      <AdminAiUsageCard />
      <AiUsageReport />

      {/* ================= USER ================= */}

      {showUsers && (
        <Card className="admin-users-card">
          <h3>{t("admin.stats.usersListTitle")}</h3>

          <table className="admin-users-table">
            <thead>
              <tr>
                <th style={{ width: "50%" }}>{t("admin.stats.colUser")}</th>
                <th style={{ width: "15%" }}>{t("admin.stats.colRole")}</th>
                <th style={{ width: "15%" }}>{t("admin.stats.colStatus")}</th>
                <th style={{ width: "20%" }}>{t("admin.stats.colOnline")}</th>
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
                        ? t("admin.stats.online")
                        : t("admin.stats.offline")}
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
                <th>{t("admin.stats.colTitle")}</th>
                <th>{t("admin.stats.colOwner")}</th>
                <th>{t("admin.stats.colVisibility")}</th>
                <th>{t("admin.stats.colModeration")}</th>
                <th>{t("admin.stats.colStorage")}</th>
                <th>{t("admin.stats.colAi")}</th>
                <th>{t("admin.stats.colCreated")}</th>
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
                    ).toLocaleDateString(locale)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="admin-stats-doc-summary">
            <span>{t("admin.stats.totalDocs", { count: documentTotalElements })}</span>
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
