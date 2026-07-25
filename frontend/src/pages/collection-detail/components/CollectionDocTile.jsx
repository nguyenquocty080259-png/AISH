import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import Card from "../../../components/ui/Card";
import Badge from "../../../components/ui/Badge";
import Button from "../../../components/ui/Button";
import { ROUTES, buildRoute } from "../../../constants/routes";

// Local tile for this page only — My Documents' DocumentCard is page-specific and not
// worth refactoring into a shared component for this single reuse (kept scope minimal).
export default function CollectionDocTile({ item, onRemove, removing }) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const handleRemoveClick = (e) => {
    e.stopPropagation();
    onRemove(item.documentId);
  };

  // DEC-034: deleted vs owner-made-private are merged into one generic, non-clickable
  // tile — never reveal which case it is.
  if (!item.available) {
    return (
      <Card className="collection-doc-tile collection-doc-tile--unavailable">
        <p className="collection-doc-tile__unavailable-text">
          {t("collectionDetail.unavailable")}
        </p>
        <Button variant="ghost" onClick={handleRemoveClick} disabled={removing}>
          {removing ? t("collectionDetail.removing") : t("collectionDetail.removeFromCollection")}
        </Button>
      </Card>
    );
  }

  const doc = item.document;

  return (
    <Card
      clickable
      className="collection-doc-tile"
      onClick={() => navigate(buildRoute(ROUTES.DOCUMENT_DETAIL, { id: doc.id }))}
    >
      <div className="collection-doc-tile__top">
        <h3 className="collection-doc-tile__title">{doc.title}</h3>
        {doc.storageType && <Badge intent="info">{doc.storageType}</Badge>}
      </div>
      <Button variant="ghost" onClick={handleRemoveClick} disabled={removing}>
        {removing ? t("collectionDetail.removing") : t("collectionDetail.removeFromCollection")}
      </Button>
    </Card>
  );
}
