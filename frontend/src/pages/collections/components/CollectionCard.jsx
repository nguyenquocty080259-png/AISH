import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import Card from "../../../components/ui/Card";
import Button from "../../../components/ui/Button";
import { ROUTES, buildRoute } from "../../../constants/routes";

// Thẻ 1 bộ sưu tập: bấm vào để mở trang chi tiết, có nút đổi tên/xoá riêng (không kích hoạt điều
// hướng khi bấm nút nhờ stopPropagation).
export default function CollectionCard({ collection, onRename, onDelete }) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const goToDetail = () => {
    navigate(buildRoute(ROUTES.COLLECTION_DETAIL, { id: collection.id }));
  };

  const stop = (fn) => (e) => {
    e.stopPropagation();
    fn(collection);
  };

  return (
    <Card clickable className="collection-card" onClick={goToDetail}>
      <h3 className="collection-card__name">{collection.name}</h3>
      <p className="collection-card__count">
        {t("collections.docCount", { count: collection.documentCount ?? 0 })}
      </p>
      <div className="collection-card__actions">
        <Button variant="ghost" onClick={stop(onRename)}>
          {t("collections.rename")}
        </Button>
        <Button variant="ghost" onClick={stop(onDelete)}>
          {t("collections.delete")}
        </Button>
      </div>
    </Card>
  );
}
