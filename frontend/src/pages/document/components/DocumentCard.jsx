import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES, buildRoute } from "../../../constants/routes";
import DocumentThumb from "../../../components/ui/DocumentThumb";
import FormatBadge from "../../../components/ui/FormatBadge";
import ReportMenu from "../../../components/report/ReportMenu";
import ModerationBadge from "./ModerationBadge";

// Một thẻ tài liệu trong lưới: ảnh thumbnail, tiêu đề (điều hướng sang trang chi tiết), mô tả,
// nhãn định dạng/kiểm duyệt, nút yêu thích và menu báo cáo vi phạm.
export default function DocumentCard({ doc, onToggleFavorite }) {
  const { t } = useTranslation();
  const detail = buildRoute(ROUTES.DOCUMENT_DETAIL, { id: doc.id });

  return (
    <article className="doc-tile doc-card">
      <div className="doc-tile__actions">
        <ReportMenu targetType="DOCUMENT" targetId={doc.id} />
        <button
          type="button"
          className={`doc-tile__fav has-custom-focus ${doc.favorited ? "doc-tile__fav--active" : ""}`.trim()}
          onClick={() => onToggleFavorite(doc.id)}
          aria-pressed={!!doc.favorited}
          aria-label={t("documents.favorite")}
        >
          <span aria-hidden="true">{doc.favorited ? "♥" : "♡"}</span>
        </button>
      </div>

      <Link to={detail} className="doc-card__thumb-link doc-tile__media" tabIndex={-1} aria-hidden="true">
        <DocumentThumb doc={doc} />
      </Link>

      <div className="doc-tile__body">
        {doc.subjectNames?.length > 0 && (
          <span className="doc-tile__eyebrow">{doc.subjectNames.join(", ")}</span>
        )}

        <Link to={detail} className="doc-tile__title has-custom-focus">{doc.title}</Link>

        {doc.description && <p className="doc-tile__desc">{doc.description}</p>}

        <div className="doc-tile__badges">
          <FormatBadge fileType={doc.fileType} fileName={doc.fileName} />
          <ModerationBadge doc={doc} />
        </div>

        <div className="doc-tile__foot">
          <span className="doc-tile__owner">{doc.ownerName}</span>
          <span className="doc-tile__stats">
            <span className="doc-tile__stat doc-tile__stat--rating">
              <span aria-hidden="true">★</span>
              {doc.averageRating?.toFixed?.(1) ?? "—"}
            </span>
            <span className="doc-tile__stat">
              <span aria-hidden="true">⬇</span>
              {doc.downloadCount ?? 0}
            </span>
          </span>
        </div>
      </div>
    </article>
  );
}
