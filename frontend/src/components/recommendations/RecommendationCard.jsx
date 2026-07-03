import Card from "../ui/Card";
import Badge from "../ui/Badge";
import "./RecommendationCard.css";

// Dùng chung cho tab "Liên quan" (Document Detail), "Gợi ý cho bạn" (Dashboard) và
// trang "Yêu thích". RecommendedDocumentDTO hiện KHÔNG có storageType -> badge storage
// chỉ hiện nếu field có mặt. actions: node tuỳ chọn (vd nút un-favorite) hiện góc trên phải.
export default function RecommendationCard({ item, onClick, actions }) {
  return (
    <Card clickable onClick={onClick} className="recommendation-card">
      <div className="recommendation-card__header">
        <h3 className="recommendation-card__title">{item.title}</h3>
        {actions && (
          <div
            className="recommendation-card__actions"
            onClick={(e) => e.stopPropagation()}
          >
            {actions}
          </div>
        )}
      </div>
      {item.storageType && (
        <Badge intent="neutral" className="recommendation-card__storage">
          {item.storageType}
        </Badge>
      )}
      {item.subjectNames?.length > 0 && (
        <p className="recommendation-card__subjects">{item.subjectNames.join(", ")}</p>
      )}
      {item.ownerName && (
        <p className="recommendation-card__owner">Đăng bởi {item.ownerName}</p>
      )}
    </Card>
  );
}
