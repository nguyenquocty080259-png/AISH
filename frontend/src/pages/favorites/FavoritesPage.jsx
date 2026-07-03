import PageHeader from "../../components/ui/PageHeader";
import EmptyState from "../../components/ui/EmptyState";
import RecommendationCard from "../../components/recommendations/RecommendationCard";
import { useFavoritesPage } from "./hooks/useFavoritesPage";
import "./favorites.css";

export default function FavoritesPage() {
  const { favorites, loading, unfavorite, goToDocument } = useFavoritesPage();

  return (
    <div className="favorites-page">
      <PageHeader title="Yêu thích" subtitle="Tài liệu bạn đã đánh dấu yêu thích." />

      {loading ? (
        <p className="favorites-page__loading">Đang tải...</p>
      ) : favorites.length === 0 ? (
        <EmptyState icon="⭐" message="Chưa có tài liệu yêu thích." />
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
                  aria-label="Bỏ yêu thích"
                  title="Bỏ yêu thích"
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
