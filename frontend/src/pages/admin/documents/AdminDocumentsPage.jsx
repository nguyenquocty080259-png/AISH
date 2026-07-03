import PageHeader from "../../../components/ui/PageHeader";
import Badge from "../../../components/ui/Badge";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
import EmptyState from "../../../components/ui/EmptyState";
import Table from "../../../components/ui/Table";
import { useAdminDocumentsPage } from "./hooks/useAdminDocumentsPage";
import "./admin-documents.css";

const VISIBILITY_BADGE = {
  PUBLIC: { intent: "info", label: "Công khai" },
  PRIVATE: { intent: "neutral", label: "Riêng tư" },
};

const MODERATION_BADGE = {
  NOT_REQUIRED: { intent: "neutral", label: "Không cần duyệt" },
  PENDING: { intent: "warning", label: "Đang chờ duyệt" },
  APPROVED: { intent: "success", label: "Đã duyệt" },
  REJECTED: { intent: "error", label: "Đã từ chối" },
};

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("vi-VN");
}

export default function AdminDocumentsPage() {
  const {
    documents,
    loading,
    page,
    totalPages,
    setPage,
    removeTarget,
    removing,
    openRemoveModal,
    closeRemoveModal,
    confirmRemove,
  } = useAdminDocumentsPage();

  return (
    <div className="admin-documents-page">
      <PageHeader
        title="Quản lý tài liệu"
        subtitle="Gỡ tài liệu vi phạm chính sách (DEC-009: đây là xử lý vi phạm, KHÔNG chuyển quyền sở hữu, KHÔNG chỉnh sửa nội dung)."
      />

      {loading ? (
        <p className="admin-documents-page__loading">Đang tải danh sách tài liệu...</p>
      ) : documents.length === 0 ? (
        <EmptyState icon="📄" message="Không có tài liệu nào." />
      ) : (
        <>
          <Table>
            <Table.Head>
              <Table.Row>
                <Table.HeaderCell>Tài liệu</Table.HeaderCell>
                <Table.HeaderCell>Chủ sở hữu</Table.HeaderCell>
                <Table.HeaderCell>Hiển thị</Table.HeaderCell>
                <Table.HeaderCell>Kiểm duyệt</Table.HeaderCell>
                <Table.HeaderCell>Lưu trữ</Table.HeaderCell>
                <Table.HeaderCell>Ngày tạo</Table.HeaderCell>
                <Table.HeaderCell />
              </Table.Row>
            </Table.Head>
            <Table.Body>
              {documents.map((doc) => {
                const visibilityBadge = VISIBILITY_BADGE[doc.visibility] ?? VISIBILITY_BADGE.PRIVATE;
                const moderationBadge =
                  MODERATION_BADGE[doc.moderationStatus] ?? MODERATION_BADGE.NOT_REQUIRED;
                return (
                  <Table.Row key={doc.id}>
                    <Table.Cell className="ui-table__truncate">{doc.title}</Table.Cell>
                    <Table.Cell>{doc.ownerName}</Table.Cell>
                    <Table.Cell>
                      <Badge intent={visibilityBadge.intent}>{visibilityBadge.label}</Badge>
                    </Table.Cell>
                    <Table.Cell>
                      <Badge intent={moderationBadge.intent}>{moderationBadge.label}</Badge>
                    </Table.Cell>
                    <Table.Cell>
                      {doc.storageType && <Badge intent="info">{doc.storageType}</Badge>}
                    </Table.Cell>
                    <Table.Cell>{formatDate(doc.createdAt)}</Table.Cell>
                    <Table.Cell>
                      <Button variant="danger" onClick={() => openRemoveModal(doc)}>
                        Gỡ vi phạm
                      </Button>
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>

          {totalPages > 1 && (
            <div className="admin-documents-page__pagination">
              <Button variant="secondary" disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>
                ‹ Trước
              </Button>
              <span>
                Trang {page + 1} / {totalPages}
              </span>
              <Button
                variant="secondary"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Sau ›
              </Button>
            </div>
          )}
        </>
      )}

      <Modal open={!!removeTarget} onClose={closeRemoveModal} title="Gỡ tài liệu vi phạm">
        <p className="admin-documents-page__confirm-text">
          Gỡ tài liệu "{removeTarget?.title}" khỏi hệ thống vì vi phạm chính sách. Đây là hành
          động xử lý vi phạm — tài liệu KHÔNG chuyển quyền sở hữu cho Admin và nội dung KHÔNG bị
          chỉnh sửa, chỉ bị gỡ khỏi hiển thị.
        </p>
        <div className="admin-documents-page__modal-actions">
          <Button variant="secondary" onClick={closeRemoveModal} disabled={removing}>
            Hủy
          </Button>
          <Button variant="danger" onClick={confirmRemove} disabled={removing}>
            {removing ? "Đang gỡ..." : "Xác nhận gỡ"}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
