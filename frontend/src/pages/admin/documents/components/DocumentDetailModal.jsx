import { useTranslation } from "react-i18next";
import Modal from "../../../../components/ui/Modal";
import Button from "../../../../components/ui/Button";
import { SkeletonText } from "../../../../components/ui/Skeleton";
import { ModerationBadge, VisibilityBadge } from "../../../../components/ui/StatusBadge";

function Row({ label, children }) {
  return (
    <div className="admin-documents-page__detail-row">
      <span className="admin-documents-page__detail-label">{label}</span>
      <span>{children}</span>
    </div>
  );
}

// Modal xem chi tiết tài liệu — chỉ đọc, không có ô chỉnh sửa nào.
export default function DocumentDetailModal({ open, doc, loading, onClose }) {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (iso) => (iso ? new Date(iso).toLocaleString(locale) : "—");
  return (
    <Modal
      open={open}
      onClose={onClose}
      title={t("admin.documents.detailTitle")}
      footer={
        <Button variant="secondary" onClick={onClose}>
          {t("common.actions.close")}
        </Button>
      }
    >
      {loading ? (
        <SkeletonText lines={8} />
      ) : doc ? (
        <div className="admin-documents-page__detail">
          <Row label={t("admin.documents.dTitle")}>{doc.title || "—"}</Row>
          <Row label={t("admin.documents.dDesc")}>{doc.description || "—"}</Row>
          <Row label={t("admin.documents.dOwner")}>{doc.ownerName || "—"}</Row>
          <Row label={t("admin.documents.dSubjects")}>{doc.subjectNames?.length ? doc.subjectNames.join(", ") : "—"}</Row>
          <Row label={t("admin.documents.dVisibility")}>
            <VisibilityBadge visibility={doc.visibility} /> {!doc.visibility && "—"}
          </Row>
          <Row label={t("admin.documents.dModeration")}>
            <ModerationBadge status={doc.moderationStatus} /> {!doc.moderationStatus && "—"}
            {doc.moderationReason ? ` — ${doc.moderationReason}` : ""}
          </Row>
          <Row label={t("admin.documents.dStorage")}>{doc.storageType || "—"}</Row>
          <Row label={t("admin.documents.dFileName")}>{doc.fileName || "—"}</Row>
          <Row label={t("admin.documents.dFavorites")}>{doc.favoriteCount ?? 0}</Row>
          <Row label={t("admin.documents.dDownloads")}>{doc.downloadCount ?? 0}</Row>
          <Row label={t("admin.documents.dAvgRating")}>{doc.averageRating != null ? doc.averageRating.toFixed(1) : "—"}</Row>
          <Row label={t("admin.documents.dCreated")}>{formatDate(doc.createdAt)}</Row>
          {doc.deletedAt && <Row label={t("admin.documents.dRemovedAt")}>{formatDate(doc.deletedAt)}</Row>}
        </div>
      ) : null}
    </Modal>
  );
}
