import { useTranslation } from "react-i18next";
import Badge from "../../components/ui/Badge";
import EmptyState from "../../components/ui/EmptyState";
import PageHeader from "../../components/ui/PageHeader";
import Table from "../../components/ui/Table";
import { useMyReportsPage } from "./hooks/useMyReportsPage";

const STATUS_BADGE = {
  PENDING: { intent: "warning", labelKey: "myReports.statusPending" },
  RESOLVED: { intent: "success", labelKey: "myReports.statusResolved" },
  DISMISSED: { intent: "neutral", labelKey: "myReports.statusDismissed" },
};

export default function MyReportsPage() {
  const { t, i18n } = useTranslation();
  const { reports, loading } = useMyReportsPage();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (value) => (value ? new Date(value).toLocaleString(locale) : "—");
  return (
    <div style={{ padding: "var(--spacing-xl)", maxWidth: "var(--container-width)", margin: "0 auto" }}>
      <PageHeader title={t("myReports.title")} subtitle={t("myReports.subtitle")} />
      {loading ? <p>{t("myReports.loading")}</p> : reports.length === 0 ?
        <EmptyState icon="📭" message={t("myReports.empty")} /> :
        <Table>
          <Table.Head><Table.Row>
            <Table.HeaderCell>{t("myReports.colTarget")}</Table.HeaderCell><Table.HeaderCell>{t("myReports.colId")}</Table.HeaderCell>
            <Table.HeaderCell>{t("myReports.colReason")}</Table.HeaderCell><Table.HeaderCell>{t("myReports.colStatus")}</Table.HeaderCell>
            <Table.HeaderCell>{t("myReports.colAdminResponse")}</Table.HeaderCell><Table.HeaderCell>{t("myReports.colDate")}</Table.HeaderCell>
          </Table.Row></Table.Head>
          <Table.Body>{reports.map((report) => {
            const badge = STATUS_BADGE[report.status] ?? STATUS_BADGE.PENDING;
            return <Table.Row key={report.id}>
              <Table.Cell>{report.targetType}</Table.Cell><Table.Cell>#{report.targetId}</Table.Cell>
              <Table.Cell className="ui-table__truncate">{report.reason || "—"}</Table.Cell>
              <Table.Cell><Badge intent={badge.intent}>{t(badge.labelKey)}</Badge></Table.Cell>
              <Table.Cell>{report.adminResponse || "—"}</Table.Cell>
              <Table.Cell>{formatDate(report.createdAt)}</Table.Cell>
            </Table.Row>;
          })}</Table.Body>
        </Table>}
    </div>
  );
}
