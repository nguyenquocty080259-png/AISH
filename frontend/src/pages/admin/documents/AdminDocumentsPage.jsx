import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
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
import { ROUTES, buildRoute } from "../../../constants/routes";
import "./admin-documents.css";

const VISIBILITY_BADGE = {
  PUBLIC: { intent: "info", labelKey: "admin.documents.visPublic" },
  PRIVATE: { intent: "neutral", labelKey: "admin.documents.visPrivate" },
};

const STATUS_BADGE = {
  ACTIVE: { intent: "success", labelKey: "admin.documents.statusActive" },
  REMOVED: { intent: "error", labelKey: "admin.documents.statusRemoved" },
};

const MODERATION_BADGE = {
  NOT_REQUIRED: { intent: "neutral", labelKey: "admin.documents.modNotRequired" },
  ADMIN_PENDING: { intent: "warning", labelKey: "admin.documents.modPending" },
  APPROVED: { intent: "success", labelKey: "admin.documents.modApproved" },
  REJECTED: { intent: "error", labelKey: "admin.documents.modRejected" },
};

// Kết quả AI pre-screen kèm theo lần chờ duyệt — chỉ có nghĩa khi đang ADMIN_PENDING.
const AI_SCREEN_LABEL = {
  PASS: "admin.documents.aiScreenPass",
  FLAG: "admin.documents.aiScreenFlag",
};

export default function AdminDocumentsPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (iso) => (iso ? new Date(iso).toLocaleString(locale) : "—");
  const {
    documents,
    loading,
    page,
    totalPages,
    setPage,
    needsReview,
    changeReviewFilter,
    keyword,
    changeKeyword,
    visibilityFilter,
    changeVisibilityFilter,
    moderationFilter,
    changeModerationFilter,
    removedFilter,
    changeRemovedFilter,
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
        title={t("admin.documents.title")}
        subtitle={t("admin.documents.subtitle")}
      />

      <div className="admin-documents-page__filters" role="group" aria-label={t("admin.documents.filterAria")}>
        <button
          type="button"
          className={!needsReview ? "admin-documents-page__filter--active" : ""}
          onClick={() => changeReviewFilter(false)}
        >
          {t("admin.documents.filterAll")}
        </button>
        <button
          type="button"
          className={needsReview ? "admin-documents-page__filter--active" : ""}
          onClick={() => changeReviewFilter(true)}
        >
          {t("admin.documents.filterNeedsReview")}
        </button>
      </div>

      {/* Tab "Cần xem xét" luôn hiện toàn bộ hàng chờ nên backend bỏ qua các bộ lọc này —
          ẩn luôn ở UI để không tạo cảm giác đã lọc mà kết quả không đổi. */}
      {!needsReview && (
        <div className="admin-documents-page__search">
          <input
            type="search"
            className="admin-documents-page__search-input"
            placeholder={t("admin.documents.searchPlaceholder")}
            value={keyword}
            onChange={(e) => changeKeyword(e.target.value)}
            aria-label={t("admin.documents.searchPlaceholder")}
          />
          <label className="admin-documents-page__filter-field">
            <span>{t("admin.documents.filterVisibilityLabel")}</span>
            <select value={visibilityFilter} onChange={(e) => changeVisibilityFilter(e.target.value)}>
              <option value="">{t("admin.documents.filterAll")}</option>
              <option value="PUBLIC">{t("admin.documents.visPublic")}</option>
              <option value="PRIVATE">{t("admin.documents.visPrivate")}</option>
            </select>
          </label>
          <label className="admin-documents-page__filter-field">
            <span>{t("admin.documents.filterModerationLabel")}</span>
            <select value={moderationFilter} onChange={(e) => changeModerationFilter(e.target.value)}>
              <option value="">{t("admin.documents.filterAll")}</option>
              <option value="NOT_REQUIRED">{t("admin.documents.modNotRequired")}</option>
              <option value="ADMIN_PENDING">{t("admin.documents.modPending")}</option>
              <option value="APPROVED">{t("admin.documents.modApproved")}</option>
              <option value="REJECTED">{t("admin.documents.modRejected")}</option>
            </select>
          </label>
          <label className="admin-documents-page__filter-field">
            <span>{t("admin.documents.filterRemovedLabel")}</span>
            <select value={removedFilter} onChange={(e) => changeRemovedFilter(e.target.value)}>
              <option value="">{t("admin.documents.filterAll")}</option>
              <option value="ACTIVE">{t("admin.documents.statusActive")}</option>
              <option value="REMOVED">{t("admin.documents.statusRemoved")}</option>
            </select>
          </label>
        </div>
      )}

      {loading ? (
        <p className="admin-documents-page__loading">{t("admin.documents.loading")}</p>
      ) : documents.length === 0 ? (
        <EmptyState icon="📄" message={t("admin.documents.empty")} />
      ) : (
        <>
          <Table>
            <Table.Head>
              <Table.Row>
                <Table.HeaderCell>{t("admin.documents.colDocument")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.documents.colOwner")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.documents.colVisibility")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.documents.colModeration")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.documents.colStorage")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.documents.colAi")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.documents.colStatus")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.documents.colCreated")}</Table.HeaderCell>
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
                const isPending = doc.moderationStatus === "ADMIN_PENDING";
                const aiScreenLabelKey = AI_SCREEN_LABEL[doc.aiScreenOutcome];
                const isReviewingThis = reviewing?.id === doc.id;
                return (
                  <Table.Row key={doc.id}>
                    <Table.Cell className="ui-table__truncate">
                      {/* Mở trang chi tiết đầy đủ (đọc nội dung + duyệt/từ chối) — Admin được
                          backend miễn trừ nên xem được cả tài liệu PRIVATE đang chờ duyệt. */}
                      <Link
                        className="admin-documents-page__doc-link"
                        to={buildRoute(ROUTES.DOCUMENT_DETAIL, { id: doc.id })}
                      >
                        {doc.title}
                      </Link>
                    </Table.Cell>
                    <Table.Cell>{doc.ownerName}</Table.Cell>
                    <Table.Cell>
                      <Badge intent={visibilityBadge.intent}>{t(visibilityBadge.labelKey)}</Badge>
                    </Table.Cell>
                    <Table.Cell>
                      <Badge intent={moderationBadge.intent}>{t(moderationBadge.labelKey)}</Badge>
                      {isPending && aiScreenLabelKey && (
                        <span className="admin-documents-page__ai-screen">{t(aiScreenLabelKey)}</span>
                      )}
                    </Table.Cell>
                    <Table.Cell>
                      {doc.storageType && <Badge intent="info">{doc.storageType}</Badge>}
                    </Table.Cell>
                    <Table.Cell><IngestStatusBadge status={doc.ingestStatus} /></Table.Cell>
                    <Table.Cell>
                      <Badge intent={statusBadge.intent}>{t(statusBadge.labelKey)}</Badge>
                    </Table.Cell>
                    <Table.Cell>{formatDate(doc.createdAt)}</Table.Cell>
                    <Table.Cell>
                      <div className="flex flex-col items-stretch gap-1.5 w-40">
                        <Button variant="secondary" onClick={() => openDetailModal(doc)}>
                          {t("admin.documents.detail")}
                        </Button>
                        <Button variant="secondary" onClick={() => openEditModal(doc)}>
                          {t("common.actions.edit")}
                        </Button>
                        {!isRemoved && (
                          <>
                            {isApproved ? (
                              <Button variant="secondary" disabled>
                                {t("admin.documents.approvedTag")}
                              </Button>
                            ) : (
                              <Button
                                variant="primary"
                                onClick={() => reviewDocument(doc, "approve")}
                                disabled={isReviewingThis}
                              >
                                {isReviewingThis && reviewing.action === "approve"
                                  ? t("admin.documents.approving")
                                  : t("admin.documents.approve")}
                              </Button>
                            )}

                            {isRejected ? (
                              <Button variant="secondary" disabled>
                                {t("admin.documents.removedTag")}
                              </Button>
                            ) : (
                              <Button
                                variant="danger"
                                onClick={() => reviewDocument(doc, "remove")}
                                disabled={isReviewingThis}
                              >
                                {isReviewingThis && reviewing.action === "remove"
                                  ? t("admin.documents.removingDoc")
                                  : t("admin.documents.remove")}
                              </Button>
                            )}
                          </>
                        )}

                        {isRemoved ? (
                          <>
                            <Button variant="secondary" disabled>
                              {t("admin.documents.removedViolationTag")}
                            </Button>
                            <Button
                              variant="primary"
                              onClick={() => restoreDocument(doc)}
                              disabled={restoringId === doc.id}
                            >
                              {restoringId === doc.id ? t("admin.documents.restoring") : t("admin.documents.restore")}
                            </Button>
                          </>
                        ) : (
                          <Button variant="danger" onClick={() => openRemoveModal(doc)}>
                            {t("admin.documents.removeViolation")}
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

      <Modal open={!!removeTarget} onClose={closeRemoveModal} title={t("admin.documents.removeTitle")}>
        <p className="admin-documents-page__confirm-text">
          {t("admin.documents.removeConfirm", { title: removeTarget?.title })}
        </p>
        <div className="admin-documents-page__modal-actions">
          <Button variant="secondary" onClick={closeRemoveModal} disabled={removing}>
            {t("common.actions.cancel")}
          </Button>
          <Button variant="danger" onClick={confirmRemove} disabled={removing}>
            {removing ? t("admin.common.removing") : t("admin.documents.confirmRemove")}
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
