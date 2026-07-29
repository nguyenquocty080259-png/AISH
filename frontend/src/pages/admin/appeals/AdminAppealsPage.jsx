import { useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PageHeader from "../../../components/ui/PageHeader";
import Badge from "../../../components/ui/Badge";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
import EmptyState from "../../../components/ui/EmptyState";
import Table from "../../../components/ui/Table";
import CaseThread from "../../../components/interaction/CaseThread";
import { useAdminAppealsPage } from "./hooks/useAdminAppealsPage";
import "./admin-appeals.css";

const STATUS_BADGE = {
  APPEAL_PENDING: { intent: "warning", labelKey: "admin.appeals.statusPending" },
  APPEAL_APPROVED: { intent: "success", labelKey: "admin.appeals.statusApproved" },
  APPEAL_REJECTED: { intent: "error", labelKey: "admin.appeals.statusRejected" },
};

export default function AdminAppealsPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (iso) => (iso ? new Date(iso).toLocaleString(locale) : "—");
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
  const [threadAppeal, setThreadAppeal] = useState(null);

  return (
    <div className="admin-appeals-page">
      <PageHeader
        title={t("admin.appeals.title")}
        subtitle={t("admin.appeals.subtitle")}
        actions={activeTab === "appeals" ? (
          <select
            className="admin-appeals-page__filter"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            <option value="">{t("admin.appeals.filterAll")}</option>
            <option value="APPEAL_PENDING">{t("admin.appeals.statusPending")}</option>
            <option value="APPEAL_APPROVED">{t("admin.appeals.statusApproved")}</option>
            <option value="APPEAL_REJECTED">{t("admin.appeals.statusRejected")}</option>
          </select>
        ) : null}
      />

      <div className="admin-appeals-tabs" role="tablist" aria-label={t("admin.appeals.tabsAria")}>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === "appeals"}
          className={activeTab === "appeals" ? "admin-appeals-tabs__item admin-appeals-tabs__item--active" : "admin-appeals-tabs__item"}
          onClick={() => setActiveTab("appeals")}
        >
          {t("admin.appeals.tabAppeals")}
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === "comments"}
          className={activeTab === "comments" ? "admin-appeals-tabs__item admin-appeals-tabs__item--active" : "admin-appeals-tabs__item"}
          onClick={() => setActiveTab("comments")}
        >
          {t("admin.appeals.tabComments")}
        </button>
      </div>

      {activeTab === "appeals" && (
        loading ? (
          <p className="admin-appeals-page__loading">{t("admin.appeals.loadingAppeals")}</p>
        ) : appeals.length === 0 ? (
          <EmptyState icon="📭" message={t("admin.appeals.emptyAppeals")} />
        ) : (
          <Table>
            <Table.Head>
              <Table.Row>
                <Table.HeaderCell>{t("admin.appeals.colDocument")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.appeals.colReason")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.appeals.colSender")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.appeals.colStatus")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.appeals.colDate")}</Table.HeaderCell>
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
                    <Table.Cell><Badge intent={badge.intent}>{t(badge.labelKey)}</Badge></Table.Cell>
                    <Table.Cell>{formatDate(appeal.createdAt)}</Table.Cell>
                    <Table.Cell>
                      <div className="ui-table__actions">
                        {isPending && (
                          <>
                            <Button variant="secondary" onClick={() => openDecisionModal(appeal, "approve")}>{t("admin.appeals.approve")}</Button>
                            <Button variant="danger" onClick={() => openDecisionModal(appeal, "reject")}>{t("admin.appeals.reject")}</Button>
                          </>
                        )}
                        <Button variant="ghost" onClick={() => setThreadAppeal(appeal)}>{t("caseThread.open")}</Button>
                      </div>
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
          <p className="admin-appeals-page__loading">{t("admin.appeals.loadingComments")}</p>
        ) : comments.length === 0 ? (
          <EmptyState icon="💬" message={t("admin.appeals.emptyComments")} />
        ) : (
          <Table>
            <Table.Head>
              <Table.Row>
                <Table.HeaderCell>{t("admin.appeals.colContent")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.appeals.colSender")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.appeals.colDoc")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.appeals.colReasonShort")}</Table.HeaderCell>
                <Table.HeaderCell>{t("admin.appeals.colDate")}</Table.HeaderCell>
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
                          {t("admin.appeals.disputeNote", { note: comment.disputeNote })}
                        </p>
                      )}
                    </Table.Cell>
                    <Table.Cell>{formatDate(comment.createdAt)}</Table.Cell>
                    <Table.Cell>
                      <div className="ui-table__actions">
                        <Button variant="secondary" disabled={pending} onClick={() => reviewComment(comment, true)}>
                          {pending ? t("admin.common.processing") : t("admin.appeals.approve")}
                        </Button>
                        <Button variant="danger" disabled={pending} onClick={() => reviewComment(comment, false)}>
                          {t("admin.appeals.reject")}
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
        title={decisionTarget?.action === "approve" ? t("admin.appeals.approveTitle") : t("admin.appeals.rejectTitle")}
      >
        <p className="admin-appeals-page__confirm-text">
          {decisionTarget?.action === "approve"
            ? t("admin.appeals.approveText", { title: decisionTarget?.appeal?.document?.title })
            : t("admin.appeals.rejectText", { title: decisionTarget?.appeal?.document?.title })}
        </p>
        <label className="admin-appeals-page__note-field">
          {t("admin.appeals.noteLabel")}
          <textarea rows={3} value={note} onChange={(event) => setNote(event.target.value)} />
        </label>
        <div className="admin-appeals-page__modal-actions">
          <Button variant="secondary" onClick={closeDecisionModal} disabled={submitting}>{t("common.actions.cancel")}</Button>
          <Button
            variant={decisionTarget?.action === "approve" ? "primary" : "danger"}
            onClick={submitDecision}
            disabled={submitting}
          >
            {submitting ? t("admin.common.processing") : decisionTarget?.action === "approve" ? t("admin.appeals.confirmApprove") : t("admin.appeals.confirmReject")}
          </Button>
        </div>
      </Modal>

      <Modal open={!!threadAppeal} onClose={() => setThreadAppeal(null)} title={t("caseThread.title")}>
        {threadAppeal && <CaseThread caseType="appeal" caseId={threadAppeal.appealId} />}
      </Modal>
    </div>
  );
}
