import { useTranslation } from "react-i18next";
import PageHeader from "../../components/ui/PageHeader";
import EmptyState from "../../components/ui/EmptyState";
import RecommendationCard from "../../components/recommendations/RecommendationCard";
import { useSharedWithMePage } from "./hooks/useSharedWithMePage";
import "./shared-with-me.css";

// Danh sách tài liệu người khác chia sẻ với tôi. Chỉ xem — không có nút Sửa/Xoá vì không phải chủ.
export default function SharedWithMePage() {
  const { t } = useTranslation();
  const { items, loading, goToDocument } = useSharedWithMePage();

  return (
    <div className="shared-page">
      <PageHeader title={t("sharedWithMe.title")} subtitle={t("sharedWithMe.subtitle")} />

      {loading ? (
        <p className="shared-page__loading">{t("sharedWithMe.loading")}</p>
      ) : items.length === 0 ? (
        <EmptyState icon="🔗" message={t("sharedWithMe.empty")} />
      ) : (
        <div className="shared-grid">
          {items.map((it) => (
            <RecommendationCard
              key={it.document.id}
              item={it.document}
              onClick={() => goToDocument(it.document.id)}
              actions={
                <span className="shared-page__by" title={t("sharedWithMe.sharedBy", { name: it.sharedByName ?? "?" })}>
                  {it.permission === "COMMENTER" ? t("sharedWithMe.permCommenter") : t("sharedWithMe.permViewer")}
                </span>
              }
            />
          ))}
        </div>
      )}
    </div>
  );
}
