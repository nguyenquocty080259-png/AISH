import Modal from "../../../../components/ui/Modal";
import Button from "../../../../components/ui/Button";

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("vi-VN");
}

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
  return (
    <Modal open={open} onClose={onClose} title="Chi tiết tài liệu">
      {loading ? (
        <p className="admin-documents-page__loading">Đang tải chi tiết tài liệu...</p>
      ) : doc ? (
        <div className="admin-documents-page__detail">
          <Row label="Tiêu đề">{doc.title || "—"}</Row>
          <Row label="Mô tả">{doc.description || "—"}</Row>
          <Row label="Chủ sở hữu">{doc.ownerName || "—"}</Row>
          <Row label="Môn học">{doc.subjectNames?.length ? doc.subjectNames.join(", ") : "—"}</Row>
          <Row label="Hiển thị">{doc.visibility || "—"}</Row>
          <Row label="Kiểm duyệt">
            {doc.moderationStatus || "—"}
            {doc.moderationReason ? ` — ${doc.moderationReason}` : ""}
          </Row>
          <Row label="Lưu trữ">{doc.storageType || "—"}</Row>
          <Row label="Tên file">{doc.fileName || "—"}</Row>
          <Row label="Yêu thích">{doc.favoriteCount ?? 0}</Row>
          <Row label="Lượt tải">{doc.downloadCount ?? 0}</Row>
          <Row label="Đánh giá TB">{doc.averageRating != null ? doc.averageRating.toFixed(1) : "—"}</Row>
          <Row label="Ngày tạo">{formatDate(doc.createdAt)}</Row>
          {doc.deletedAt && <Row label="Ngày gỡ">{formatDate(doc.deletedAt)}</Row>}
        </div>
      ) : null}

      <div className="admin-documents-page__modal-actions">
        <Button variant="secondary" onClick={onClose}>
          Đóng
        </Button>
      </div>
    </Modal>
  );
}
