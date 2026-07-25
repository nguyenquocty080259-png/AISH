import { useTranslation } from "react-i18next";
import Badge from "../../../components/ui/Badge";
import Button from "../../../components/ui/Button";
import EmptyState from "../../../components/ui/EmptyState";
import Modal from "../../../components/ui/Modal";
import PageHeader from "../../../components/ui/PageHeader";
import Table from "../../../components/ui/Table";
import { useAdminReportsPage } from "./hooks/useAdminReportsPage";

const STATUS_BADGE = {
  PENDING: { intent: "warning", labelKey: "admin.reports.statusPending" },
  RESOLVED: { intent: "success", labelKey: "admin.reports.statusResolved" },
  DISMISSED: { intent: "neutral", labelKey: "admin.reports.statusDismissed" },
};
const ACTION_LABEL_KEYS = {
  REMOVE_CONTENT: "admin.reports.actionRemoveContent",
  WARN_USER: "admin.reports.actionWarnUser",
  LOCK_ACCOUNT: "admin.reports.actionLockAccount",
  DISMISSED: "admin.reports.actionDismiss",
};

export default function AdminReportsPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (value) => (value ? new Date(value).toLocaleString(locale) : "—");
  const page = useAdminReportsPage();
  const actions = ["DOCUMENT", "COMMENT"].includes(page.resolveTarget?.targetType)
    ? ["REMOVE_CONTENT", "WARN_USER", "LOCK_ACCOUNT", "DISMISSED"]
    : ["WARN_USER", "LOCK_ACCOUNT", "DISMISSED"];
  return <div>
    <PageHeader title={t("admin.reports.title")} subtitle={t("admin.reports.subtitle")}
      actions={<select value={page.statusFilter} onChange={(e) => page.setStatusFilter(e.target.value)}>
        <option value="">{t("admin.reports.filterAll")}</option><option value="PENDING">PENDING</option>
        <option value="RESOLVED">RESOLVED</option><option value="DISMISSED">DISMISSED</option>
      </select>} />
    {page.loading ? <p>{t("admin.reports.loading")}</p> : page.reports.length === 0 ?
      <EmptyState icon="📭" message={t("admin.reports.empty")} /> : <Table>
        <Table.Head><Table.Row><Table.HeaderCell>{t("admin.reports.colReporter")}</Table.HeaderCell>
          <Table.HeaderCell>{t("admin.reports.colTarget")}</Table.HeaderCell><Table.HeaderCell>{t("admin.reports.colId")}</Table.HeaderCell>
          <Table.HeaderCell>{t("admin.reports.colReason")}</Table.HeaderCell><Table.HeaderCell>{t("admin.reports.colStatus")}</Table.HeaderCell>
          <Table.HeaderCell>{t("admin.reports.colDate")}</Table.HeaderCell><Table.HeaderCell /></Table.Row></Table.Head>
        <Table.Body>{page.reports.map((report) => {
          const badge = STATUS_BADGE[report.status] ?? STATUS_BADGE.PENDING;
          return <Table.Row key={report.id}><Table.Cell>{report.reporterEmail || t("admin.reports.systemReporter")}</Table.Cell>
            <Table.Cell>
              <span>{report.targetType}</span>
              {report.flaggedMessageContent && <em style={{ display: "block", marginTop: 4 }}>
                {t("admin.reports.flaggedContent", { content: report.flaggedMessageContent })}
              </em>}
            </Table.Cell><Table.Cell>#{report.targetId}</Table.Cell>
            <Table.Cell className="ui-table__truncate">{report.reason || "—"}</Table.Cell>
            <Table.Cell><Badge intent={badge.intent}>{t(badge.labelKey)}</Badge></Table.Cell>
            <Table.Cell>{formatDate(report.createdAt)}</Table.Cell><Table.Cell>
              {report.status === "PENDING" && <Button variant="secondary" onClick={() => page.openResolveModal(report)}>{t("admin.reports.resolve")}</Button>}
            </Table.Cell></Table.Row>;
        })}</Table.Body>
      </Table>}
    <Modal open={!!page.resolveTarget} onClose={page.closeResolveModal} title={t("admin.reports.resolveTitle")}>
      <p>{page.resolveTarget?.targetType} #{page.resolveTarget?.targetId}</p>
      {page.resolveTarget?.flaggedMessageContent && <p><em>
        {t("admin.reports.flaggedContent", { content: page.resolveTarget.flaggedMessageContent })}
      </em></p>}
      <label style={{ display: "grid", gap: 6, marginBottom: 16 }}>{t("admin.reports.actionLabel")}
        <select value={page.actionTaken} onChange={(e) => page.setActionTaken(e.target.value)}>
          <option value="">{t("admin.reports.chooseAction")}</option>
          {actions.map((action) => <option key={action} value={action}>{t(ACTION_LABEL_KEYS[action])}</option>)}
        </select></label>
      <label style={{ display: "grid", gap: 6 }}>{t("admin.reports.adminResponseLabel")}
        <textarea rows={4} value={page.adminResponse} onChange={(e) => page.setAdminResponse(e.target.value)} />
      </label>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 20 }}>
        <Button variant="secondary" onClick={page.closeResolveModal} disabled={page.submitting}>{t("common.actions.cancel")}</Button>
        <Button onClick={page.submitResolve} disabled={page.submitting || !page.actionTaken}>
          {page.submitting ? t("admin.common.processing") : t("admin.reports.confirm")}</Button>
      </div>
    </Modal>
  </div>;
}
