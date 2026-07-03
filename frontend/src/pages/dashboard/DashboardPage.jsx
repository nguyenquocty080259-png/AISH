import { Link, useNavigate } from "react-router-dom";
import { useDashboardPage } from "./hooks/useDashboardPage";
import { ROUTES, buildRoute } from "../../constants/routes";
import EmptyState from "../../components/ui/EmptyState";
import RecommendationCard from "../../components/recommendations/RecommendationCard";
import "./dashboard.css";

function formatRelativeTime(isoString) {
  if (!isoString) return "";
  const rtf = new Intl.RelativeTimeFormat("vi", { numeric: "auto" });
  const diffMs = new Date(isoString).getTime() - Date.now();
  const diffMinutes = Math.round(diffMs / 60000);
  if (Math.abs(diffMinutes) < 60) return rtf.format(diffMinutes, "minute");
  const diffHours = Math.round(diffMinutes / 60);
  if (Math.abs(diffHours) < 24) return rtf.format(diffHours, "hour");
  const diffDays = Math.round(diffHours / 24);
  return rtf.format(diffDays, "day");
}

export default function DashboardPage() {
  const { user, handleLogout, collections, recentlyViewed, recommendations, loading } =
    useDashboardPage();
  const navigate = useNavigate();

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

      <section className="dashboard__section">
        <h2 className="dashboard__section-title">Spaces</h2>
        {loading ? (
          <p className="dashboard__section-loading">Đang tải...</p>
        ) : collections.length === 0 ? (
          <p className="dashboard__section-empty">Chưa có collection nào.</p>
        ) : (
          <div className="dashboard__spaces">
            {collections.map((collection) => (
              <Link
                key={collection.id}
                to={buildRoute(ROUTES.COLLECTION_DETAIL, { id: collection.id })}
                className="space-card"
              >
                <h3 className="space-card__name">{collection.name}</h3>
                <p className="space-card__count">
                  {collection.documentCount ?? 0} tài liệu
                </p>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="dashboard__section">
        <h2 className="dashboard__section-title">Tài liệu gần đây</h2>
        {loading ? (
          <p className="dashboard__section-loading">Đang tải...</p>
        ) : recentlyViewed.length === 0 ? (
          <p className="dashboard__section-empty">
            Chưa có tài liệu nào được xem gần đây.
          </p>
        ) : (
          <ul className="dashboard__recent-list">
            {recentlyViewed.map((item) => (
              <li key={item.documentId} className="recent-row">
                <Link
                  to={buildRoute(ROUTES.DOCUMENT_DETAIL, { id: item.documentId })}
                  className="recent-row__link"
                >
                  <span className="recent-row__title">
                    {item.document?.title}
                  </span>
                  <span
                    className={`recent-row__badge recent-row__badge--${(
                      item.document?.storageType ?? ""
                    ).toLowerCase()}`}
                  >
                    {item.document?.storageType}
                  </span>
                  <span className="recent-row__time">
                    {formatRelativeTime(item.viewedAt)}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="dashboard__section">
        <h2 className="dashboard__section-title">Gợi ý cho bạn</h2>
        {loading ? (
          <p className="dashboard__section-loading">Đang tải...</p>
        ) : recommendations.length === 0 ? (
          <EmptyState
            icon="✨"
            message="Chưa có gợi ý — hãy tải lên hoặc xem vài tài liệu"
          />
        ) : (
          <div className="dashboard__recommendations">
            {recommendations.map((item) => (
              <RecommendationCard
                key={item.documentId}
                item={item}
                onClick={() =>
                  navigate(buildRoute(ROUTES.DOCUMENT_DETAIL, { id: item.documentId }))
                }
              />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
