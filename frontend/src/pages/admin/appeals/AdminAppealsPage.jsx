import { Link } from "react-router-dom";
import PageHeader from "../../../components/ui/PageHeader";
import Badge from "../../../components/ui/Badge";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
import EmptyState from "../../../components/ui/EmptyState";
import Table from "../../../components/ui/Table";
import { useAdminAppealsPage } from "./hooks/useAdminAppealsPage";
import "./admin-appeals.css";

const STATUS_BADGE = {
  APPEAL_PENDING: { intent: "warning", label: "Đang chờ" },
  APPEAL_APPROVED: { intent: "success", label: "Đã duyệt" },
  APPEAL_REJECTED: { intent: "error", label: "Đã từ chối" },
};

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("vi-VN");
}

export default function AdminAppealsPage() {
  const {
    activeTab,
    setActiveTab,
    appeals,
    loading,
    statusFilter,
    setStatusFilter,
    decisionTarget,
    note,
    setNote,
    submitting,
    openDecisionModal,
    closeDecisionModal,
    submitDecision,
    comments,
    commentsLoading,
    commentPendingIds,
    reviewComment,
  } = useAdminAppealsPage();

  return (
    <div className="admin-appeals-page">
      <PageHeader
        title="Kháng nghị và bình luận"
        subtitle="Xử lý kháng nghị tài liệu và các bình luận đang chờ kiểm duyệt."
        actions={activeTab === "appeals" ? (
          <select
            className="admin-appeals-page__filter"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            <option value="">Tất cả</option>
            <option value="APPEAL_PENDING">Đang chờ</option>
            <option value="APPEAL_APPROVED">Đã duyệt</option>
            <option value="APPEAL_REJECTED">Đã từ chối</option>
          </select>
        ) : null}
      />

      <div className="admin-appeals-tabs" role="tablist" aria-label="Loại nội dung cần xử lý">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === "appeals"}
          className={activeTab === "appeals" ? "admin-appeals-tabs__item admin-appeals-tabs__item--active" : "admin-appeals-tabs__item"}
          onClick={() => setActiveTab("appeals")}
        >
          Kháng nghị tài liệu
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === "comments"}
          className={activeTab === "comments" ? "admin-appeals-tabs__item admin-appeals-tabs__item--active" : "admin-appeals-tabs__item"}
          onClick={() => setActiveTab("comments")}
        >
          Bình luận
        </button>
      </div>

      {activeTab === "appeals" && (
        loading ? (
          <p className="admin-appeals-page__loading">Đang tải danh sách kháng nghị...</p>
        ) : appeals.length === 0 ? (
          <EmptyState icon="📭" message="Không có kháng nghị nào." />
        ) : (
          <Table>
            <Table.Head>
              <Table.Row>
                <Table.HeaderCell>Tài liệu</Table.HeaderCell>
                <Table.HeaderCell>Lý do kháng nghị</Table.HeaderCell>
                <Table.HeaderCell>Người gửi</Table.HeaderCell>
                <Table.HeaderCell>Trạng thái</Table.HeaderCell>
                <Table.HeaderCell>Ngày gửi</Table.HeaderCell>
                <Table.HeaderCell />
              </Table.Row>
            </Table.Head>
            <Table.Body>
              {appeals.map((appeal) => {
                const badge = STATUS_BADGE[appeal.status] ?? STATUS_BADGE.APPEAL_PENDING;
                const isPending = appeal.status === "APPEAL_PENDING";
                return (
                  <Table.Row key={appeal.appealId}>
                    <Table.Cell>{appeal.document?.title}</Table.Cell>
                    <Table.Cell className="ui-table__truncate">{appeal.reason}</Table.Cell>
                    <Table.Cell>{appeal.appellantName}</Table.Cell>
                    <Table.Cell><Badge intent={badge.intent}>{badge.label}</Badge></Table.Cell>
                    <Table.Cell>{formatDate(appeal.createdAt)}</Table.Cell>
                    <Table.Cell>
                      {isPending && (
                        <div className="ui-table__actions">
                          <Button variant="secondary" onClick={() => openDecisionModal(appeal, "approve")}>Duyệt</Button>
                          <Button variant="danger" onClick={() => openDecisionModal(appeal, "reject")}>Từ chối</Button>
                        </div>
                      )}
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>
        )
      )}

      {activeTab === "comments" && (
        commentsLoading ? (
          <p className="admin-appeals-page__loading">Đang tải bình luận chờ duyệt...</p>
        ) : comments.length === 0 ? (
          <EmptyState icon="💬" message="Không có bình luận nào đang chờ duyệt." />
        ) : (
          <Table>
            <Table.Head>
              <Table.Row>
                <Table.HeaderCell>Nội dung</Table.HeaderCell>
                <Table.HeaderCell>Người gửi</Table.HeaderCell>
                <Table.HeaderCell>Tài liệu</Table.HeaderCell>
                <Table.HeaderCell>Lý do</Table.HeaderCell>
                <Table.HeaderCell>Ngày gửi</Table.HeaderCell>
                <Table.HeaderCell />
              </Table.Row>
            </Table.Head>
            <Table.Body>
              {comments.map((comment) => {
                const pending = commentPendingIds.has(comment.id);
                return (
                  <Table.Row key={comment.id}>
                    <Table.Cell className="admin-appeals-page__comment-content">{comment.content}</Table.Cell>
                    <Table.Cell>{comment.authorName}</Table.Cell>
                    <Table.Cell>
                      <Link to={`/documents/${comment.documentId}`}>{comment.documentTitle}</Link>
                    </Table.Cell>
                    <Table.Cell>
                      <p>{comment.moderationReason || "—"}</p>
                      {comment.disputeNote && (
                        <p className="admin-appeals-page__dispute-note">
                          Khiếu nại: {comment.disputeNote}
                        </p>
                      )}
                    </Table.Cell>
                    <Table.Cell>{formatDate(comment.createdAt)}</Table.Cell>
                    <Table.Cell>
                      <div className="ui-table__actions">
                        <Button variant="secondary" disabled={pending} onClick={() => reviewComment(comment, true)}>
                          {pending ? "Đang xử lý..." : "Duyệt"}
                        </Button>
                        <Button variant="danger" disabled={pending} onClick={() => reviewComment(comment, false)}>
                          Từ chối
                        </Button>
                      </div>
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>
        )
      )}

      <Modal
        open={!!decisionTarget}
        onClose={closeDecisionModal}
        title={decisionTarget?.action === "approve" ? "Duyệt kháng nghị" : "Từ chối kháng nghị"}
      >
        <p className="admin-appeals-page__confirm-text">
          Tài liệu "{decisionTarget?.appeal?.document?.title}" —{" "}
          {decisionTarget?.action === "approve"
            ? "duyệt sẽ chuyển tài liệu về công khai."
            : "từ chối sẽ giữ nguyên trạng thái hiện tại của tài liệu."}
        </p>
        <label className="admin-appeals-page__note-field">
          Ghi chú (tuỳ chọn)
          <textarea rows={3} value={note} onChange={(event) => setNote(event.target.value)} />
        </label>
        <div className="admin-appeals-page__modal-actions">
          <Button variant="secondary" onClick={closeDecisionModal} disabled={submitting}>Hủy</Button>
          <Button
            variant={decisionTarget?.action === "approve" ? "primary" : "danger"}
            onClick={submitDecision}
            disabled={submitting}
          >
            {submitting ? "Đang xử lý..." : decisionTarget?.action === "approve" ? "Xác nhận duyệt" : "Xác nhận từ chối"}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
