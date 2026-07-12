import Badge from "../../components/ui/Badge";
import EmptyState from "../../components/ui/EmptyState";
import PageHeader from "../../components/ui/PageHeader";
import Table from "../../components/ui/Table";
import { useMyReportsPage } from "./hooks/useMyReportsPage";

const STATUS_BADGE = {
  PENDING: { intent: "warning", label: "Đang chờ" },
  RESOLVED: { intent: "success", label: "Đã xử lý" },
  DISMISSED: { intent: "neutral", label: "Đã bỏ qua" },
};

const formatDate = (value) => value ? new Date(value).toLocaleString("vi-VN") : "—";

export default function MyReportsPage() {
  const { reports, loading } = useMyReportsPage();
  return (
    <div style={{ padding: "var(--spacing-xl)", maxWidth: "var(--container-width)", margin: "0 auto" }}>
      <PageHeader title="Báo cáo của tôi" subtitle="Theo dõi trạng thái các báo cáo bạn đã gửi." />
      {loading ? <p>Đang tải danh sách báo cáo...</p> : reports.length === 0 ?
        <EmptyState icon="📭" message="Bạn chưa gửi báo cáo nào." /> :
        <Table>
          <Table.Head><Table.Row>
            <Table.HeaderCell>Đối tượng</Table.HeaderCell><Table.HeaderCell>ID</Table.HeaderCell>
            <Table.HeaderCell>Lý do</Table.HeaderCell><Table.HeaderCell>Trạng thái</Table.HeaderCell>
            <Table.HeaderCell>Phản hồi Admin</Table.HeaderCell><Table.HeaderCell>Ngày gửi</Table.HeaderCell>
          </Table.Row></Table.Head>
          <Table.Body>{reports.map((report) => {
            const badge = STATUS_BADGE[report.status] ?? STATUS_BADGE.PENDING;
            return <Table.Row key={report.id}>
              <Table.Cell>{report.targetType}</Table.Cell><Table.Cell>#{report.targetId}</Table.Cell>
              <Table.Cell className="ui-table__truncate">{report.reason || "—"}</Table.Cell>
              <Table.Cell><Badge intent={badge.intent}>{badge.label}</Badge></Table.Cell>
              <Table.Cell>{report.adminResponse || "—"}</Table.Cell>
              <Table.Cell>{formatDate(report.createdAt)}</Table.Cell>
            </Table.Row>;
          })}</Table.Body>
        </Table>}
    </div>
  );
}
