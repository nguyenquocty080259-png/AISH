import PageHeader from "../../../components/ui/PageHeader";
import Badge from "../../../components/ui/Badge";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
import EmptyState from "../../../components/ui/EmptyState";
import Table from "../../../components/ui/Table";
import { useAdminDocumentsPage } from "./hooks/useAdminDocumentsPage";
import IngestStatusBadge from "./components/IngestStatusBadge";
import EditDocumentModal from "./components/EditDocumentModal";
import DocumentDetailModal from "./components/DocumentDetailModal";
import AdminPagination from "../components/AdminPagination";
import "./admin-documents.css";

const VISIBILITY_BADGE = {
  PUBLIC: { intent: "info", label: "Công khai" },
  PRIVATE: { intent: "neutral", label: "Riêng tư" },
};

const STATUS_BADGE = {
  ACTIVE: { intent: "success", label: "Đang hoạt động" },
  REMOVED: { intent: "error", label: "Đã gỡ" },
};

const MODERATION_BADGE = {
  NOT_REQUIRED: { intent: "neutral", label: "Không cần duyệt" },
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
    needsReview,
    changeReviewFilter,
    removeTarget,
    removing,
    openRemoveModal,
    closeRemoveModal,
    confirmRemove,
    editTarget,
    editing,
    loadingEditTarget,
    subjects,
    openEditModal,
    closeEditModal,
    submitEdit,
    restoringId,
    restoreDocument,
    reviewing,
    reviewDocument,
    detailTarget,
    loadingDetail,
    openDetailModal,
    closeDetailModal,
  } = useAdminDocumentsPage();

  return (
    <div className="admin-documents-page">
      <PageHeader
        title="Quản lý tài liệu"
        subtitle="Gỡ tài liệu vi phạm chính sách (DEC-009: đây là xử lý vi phạm, KHÔNG chuyển quyền sở hữu, KHÔNG chỉnh sửa nội dung)."
      />

      <div className="admin-documents-page__filters" role="group" aria-label="Lọc tài liệu">
        <button
          type="button"
          className={!needsReview ? "admin-documents-page__filter--active" : ""}
          onClick={() => changeReviewFilter(false)}
        >
          Tất cả
        </button>
        <button
          type="button"
          className={needsReview ? "admin-documents-page__filter--active" : ""}
          onClick={() => changeReviewFilter(true)}
        >
          Cần xem xét
        </button>
      </div>

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
                <Table.HeaderCell>AI</Table.HeaderCell>
                <Table.HeaderCell>Trạng thái</Table.HeaderCell>
                <Table.HeaderCell>Ngày tạo</Table.HeaderCell>
                <Table.HeaderCell />
              </Table.Row>
            </Table.Head>
            <Table.Body>
              {documents.map((doc) => {
                const visibilityBadge = VISIBILITY_BADGE[doc.visibility] ?? VISIBILITY_BADGE.PRIVATE;
                const moderationBadge =
                  MODERATION_BADGE[doc.moderationStatus] ?? MODERATION_BADGE.NOT_REQUIRED;
                const isRemoved = !!doc.deletedAt;
                const statusBadge = isRemoved ? STATUS_BADGE.REMOVED : STATUS_BADGE.ACTIVE;
                const isApproved = doc.moderationStatus === "APPROVED";
                const isRejected = doc.moderationStatus === "REJECTED";
                const isReviewingThis = reviewing?.id === doc.id;
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
                    <Table.Cell><IngestStatusBadge status={doc.ingestStatus} /></Table.Cell>
                    <Table.Cell>
                      <Badge intent={statusBadge.intent}>{statusBadge.label}</Badge>
                    </Table.Cell>
                    <Table.Cell>{formatDate(doc.createdAt)}</Table.Cell>
                    <Table.Cell>
                      <div className="flex flex-col items-stretch gap-1.5 w-40">
                        <Button variant="secondary" onClick={() => openDetailModal(doc)}>
                          Chi tiết
                        </Button>
                        <Button variant="secondary" onClick={() => openEditModal(doc)}>
                          Sửa
                        </Button>
                        {!isRemoved && (
                          <>
                            {isApproved ? (
                              <Button variant="secondary" disabled>
                                ✓ Đã duyệt
                              </Button>
                            ) : (
                              <Button
                                variant="primary"
                                onClick={() => reviewDocument(doc, "approve")}
                                disabled={isReviewingThis}
                              >
                                {isReviewingThis && reviewing.action === "approve"
                                  ? "Đang duyệt..."
                                  : "Duyệt"}
                              </Button>
                            )}

                            {isRejected ? (
                              <Button variant="secondary" disabled>
                                ✓ Đã gỡ
                              </Button>
                            ) : (
                              <Button
                                variant="danger"
                                onClick={() => reviewDocument(doc, "remove")}
                                disabled={isReviewingThis}
                              >
                                {isReviewingThis && reviewing.action === "remove"
                                  ? "Đang gỡ..."
                                  : "Gỡ"}
                              </Button>
                            )}
                          </>
                        )}

                        {isRemoved ? (
                          <>
                            <Button variant="secondary" disabled>
                              ✓ Đã gỡ vi phạm
                            </Button>
                            <Button
                              variant="primary"
                              onClick={() => restoreDocument(doc)}
                              disabled={restoringId === doc.id}
                            >
                              {restoringId === doc.id ? "Đang khôi phục..." : "Khôi phục"}
                            </Button>
                          </>
                        ) : (
                          <Button variant="danger" onClick={() => openRemoveModal(doc)}>
                            Gỡ vi phạm
                          </Button>
                        )}
                      </div>
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>

          <AdminPagination
            page={page}
            totalPages={totalPages}
            onPrev={() => setPage((currentPage) => currentPage - 1)}
            onNext={() => setPage((currentPage) => currentPage + 1)}
          />
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

      <EditDocumentModal
        open={!!editTarget}
        doc={editTarget}
        subjects={subjects}
        submitting={editing}
        loading={loadingEditTarget}
        onClose={closeEditModal}
        onSubmit={submitEdit}
      />

      <DocumentDetailModal
        open={!!detailTarget}
        doc={detailTarget}
        loading={loadingDetail}
        onClose={closeDetailModal}
      />
    </div>
  );
}
