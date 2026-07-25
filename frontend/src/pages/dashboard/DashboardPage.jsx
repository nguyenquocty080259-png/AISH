import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useDashboardPage } from "./hooks/useDashboardPage";
import { ROUTES, buildRoute } from "../../constants/routes";
import EmptyState from "../../components/ui/EmptyState";
import RecommendationCard from "../../components/recommendations/RecommendationCard";
import "./dashboard.css";

function formatRelativeTime(isoString, locale) {
  if (!isoString) return "";
  const rtf = new Intl.RelativeTimeFormat(locale || "vi", { numeric: "auto" });
  const diffMs = new Date(isoString).getTime() - Date.now();
  const diffMinutes = Math.round(diffMs / 60000);
  if (Math.abs(diffMinutes) < 60) return rtf.format(diffMinutes, "minute");
  const diffHours = Math.round(diffMinutes / 60);
  if (Math.abs(diffHours) < 24) return rtf.format(diffHours, "hour");
  const diffDays = Math.round(diffHours / 24);
  return rtf.format(diffDays, "day");
}

export default function DashboardPage() {
  const { t, i18n } = useTranslation();
  const { user, handleLogout, collections, recentlyViewed, recommendations, loading } =
    useDashboardPage();
  const navigate = useNavigate();
  const locale = i18n.resolvedLanguage || i18n.language || "vi";

  return (
    <div className="dashboard">
      <div className="dashboard__header">
        <div>
          <h1 className="dashboard__greeting">
            {t("dashboard.greeting", { name: user?.fullName ?? t("dashboard.guestName") })}
          </h1>
          <p className="dashboard__sub">{user?.email}</p>
        </div>
        <button className="dashboard__logout" onClick={handleLogout}>
          {t("common.menu.logout")}
        </button>
      </div>

      <div className="dashboard__grid">
        <Link to={ROUTES.DOCUMENTS} className="dashboard__card">
          <p className="dashboard__card-icon">📚</p>
          <h3 className="dashboard__card-title">{t("dashboard.cards.documentsTitle")}</h3>
          <p className="dashboard__card-desc">
            {t("dashboard.cards.documentsDesc")}
          </p>
        </Link>

        <Link to={ROUTES.AI_CHAT} className="dashboard__card">
          <p className="dashboard__card-icon">🤖</p>
          <h3 className="dashboard__card-title">{t("dashboard.cards.aiChatTitle")}</h3>
          <p className="dashboard__card-desc">
            {t("dashboard.cards.aiChatDesc")}
          </p>
        </Link>

        <Link to={ROUTES.PROFILE} className="dashboard__card">
          <p className="dashboard__card-icon">👤</p>
          <h3 className="dashboard__card-title">{t("dashboard.cards.profileTitle")}</h3>
          <p className="dashboard__card-desc">
            {t("dashboard.cards.profileDesc")}
          </p>
        </Link>
      </div>

      <section className="dashboard__section">
        <h2 className="dashboard__section-title">{t("dashboard.spacesTitle")}</h2>
        {loading ? (
          <p className="dashboard__section-loading">{t("common.actions.loading")}</p>
        ) : collections.length === 0 ? (
          <p className="dashboard__section-empty">{t("dashboard.noCollections")}</p>
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
                  {t("dashboard.docCount", { count: collection.documentCount ?? 0 })}
                </p>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="dashboard__section">
        <h2 className="dashboard__section-title">{t("dashboard.recentTitle")}</h2>
        {loading ? (
          <p className="dashboard__section-loading">{t("common.actions.loading")}</p>
        ) : recentlyViewed.length === 0 ? (
          <p className="dashboard__section-empty">
            {t("dashboard.noRecent")}
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
                    {formatRelativeTime(item.viewedAt, locale)}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="dashboard__section">
        <h2 className="dashboard__section-title">{t("dashboard.suggestTitle")}</h2>
        {loading ? (
          <p className="dashboard__section-loading">{t("common.actions.loading")}</p>
        ) : recommendations.length === 0 ? (
          <EmptyState
            icon="✨"
            message={t("dashboard.noSuggest")}
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
