import { Link } from "react-router-dom";
import { ROUTES, buildRoute } from "../../../constants/routes";
import DocumentThumb from "../../../components/ui/DocumentThumb";

export default function DocumentCard({ doc, onToggleFavorite }) {
  return (
    <div className="doc-card">
      <div className="doc-card__top">
        {doc.subjectNames?.length > 0 && (
          <span className="doc-card__subject">{doc.subjectNames.join(", ")}</span>
        )}
        <button
          type="button"
          className={`doc-card__fav ${doc.favorited ? "doc-card__fav--active" : ""}`}
          onClick={() => onToggleFavorite(doc.id)}
          aria-label="Yêu thích"
        >
          {doc.favorited ? "♥" : "♡"}
        </button>
      </div>

      <Link to={buildRoute(ROUTES.DOCUMENT_DETAIL, { id: doc.id })} className="doc-card__thumb-link">
        <DocumentThumb doc={doc} />
      </Link>

      <Link
        to={buildRoute(ROUTES.DOCUMENT_DETAIL, { id: doc.id })}
        className="doc-card__title"
      >
        {doc.title}
      </Link>
      <p className="doc-card__desc">{doc.description}</p>

      <div className="doc-card__meta">
        <span>{doc.ownerName}</span>
        <span>★ {doc.averageRating?.toFixed?.(1) ?? "—"}</span>
        <span>⬇ {doc.downloadCount ?? 0}</span>
      </div>
    </div>
  );
}