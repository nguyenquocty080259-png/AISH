import PageHeader from "../../components/ui/PageHeader";
import EmptyState from "../../components/ui/EmptyState";
import RecommendationCard from "../../components/recommendations/RecommendationCard";
import { useSharedWithMePage } from "./hooks/useSharedWithMePage";
import "./shared-with-me.css";

// Danh sách tài liệu người khác chia sẻ với tôi. Chỉ xem — không có nút Sửa/Xoá vì không phải chủ.
export default function SharedWithMePage() {
  const { items, loading, goToDocument } = useSharedWithMePage();

  return (
    <div className="shared-page">
      <PageHeader title="Được chia sẻ với tôi" subtitle="Tài liệu người khác đã chia sẻ cho bạn." />

      {loading ? (
        <p className="shared-page__loading">Đang tải...</p>
      ) : items.length === 0 ? (
        <EmptyState icon="🔗" message="Chưa có tài liệu nào được chia sẻ với bạn." />
      ) : (
        <div className="shared-grid">
          {items.map((it) => (
            <RecommendationCard
              key={it.document.id}
              item={it.document}
              onClick={() => goToDocument(it.document.id)}
              actions={
                <span className="shared-page__by" title={`Chia sẻ bởi ${it.sharedByName ?? "?"}`}>
                  {it.permission === "COMMENTER" ? "Bình luận" : "Xem"}
                </span>
              }
            />
          ))}
        </div>
      )}
    </div>
  );
}
