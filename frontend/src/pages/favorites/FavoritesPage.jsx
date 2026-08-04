import { useTranslation } from "react-i18next";
import PageHeader from "../../components/ui/PageHeader";
import EmptyState from "../../components/ui/EmptyState";
import RecommendationCard from "../../components/recommendations/RecommendationCard";
import { useFavoritesPage } from "./hooks/useFavoritesPage";
import "./favorites.css";

// Trang YÊU THÍCH: lưới tài liệu đã yêu thích, bấm tim để bỏ thích ngay tại trang.
export default function FavoritesPage() {
  const { t } = useTranslation();
  const { favorites, loading, unfavorite, goToDocument } = useFavoritesPage();

  return (
    <div className="favorites-page">
      <PageHeader title={t("favorites.title")} subtitle={t("favorites.subtitle")} />

      {loading ? (
        <p className="favorites-page__loading">{t("favorites.loading")}</p>
      ) : favorites.length === 0 ? (
        <EmptyState icon="⭐" message={t("favorites.empty")} />
      ) : (
        <div className="favorites-grid">
          {favorites.map((doc) => (
            <RecommendationCard
              key={doc.id}
              item={doc}
              onClick={() => goToDocument(doc.id)}
              actions={
                <button
                  type="button"
                  className="favorites-page__unfav"
                  onClick={() => unfavorite(doc.id)}
                  aria-label={t("favorites.unfavorite")}
                  title={t("favorites.unfavorite")}
                >
                  ♥
                </button>
              }
            />
          ))}
        </div>
      )}
    </div>
  );
}
