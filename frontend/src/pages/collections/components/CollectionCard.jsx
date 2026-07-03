import { useNavigate } from "react-router-dom";
import Card from "../../../components/ui/Card";
import Button from "../../../components/ui/Button";
import { ROUTES, buildRoute } from "../../../constants/routes";

export default function CollectionCard({ collection, onRename, onDelete }) {
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
        {collection.documentCount ?? 0} tài liệu
      </p>
      <div className="collection-card__actions">
        <Button variant="ghost" onClick={stop(onRename)}>
          Đổi tên
        </Button>
        <Button variant="ghost" onClick={stop(onDelete)}>
          Xóa
        </Button>
      </div>
    </Card>
  );
}
