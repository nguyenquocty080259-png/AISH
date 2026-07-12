import Badge from "../../../components/ui/Badge";
import Button from "../../../components/ui/Button";
import EmptyState from "../../../components/ui/EmptyState";
import Modal from "../../../components/ui/Modal";
import PageHeader from "../../../components/ui/PageHeader";
import Table from "../../../components/ui/Table";
import { useAdminReportsPage } from "./hooks/useAdminReportsPage";

const STATUS_BADGE = {
  PENDING: { intent: "warning", label: "Đang chờ" },
  RESOLVED: { intent: "success", label: "Đã xử lý" },
  DISMISSED: { intent: "neutral", label: "Đã bỏ qua" },
};
const ACTION_LABELS = { REMOVE_CONTENT: "Gỡ nội dung", WARN_USER: "Cảnh báo người dùng",
  LOCK_ACCOUNT: "Khóa tài khoản", DISMISSED: "Bỏ qua báo cáo" };
const formatDate = (value) => value ? new Date(value).toLocaleString("vi-VN") : "—";

export default function AdminReportsPage() {
  const page = useAdminReportsPage();
  const actions = ["DOCUMENT", "COMMENT"].includes(page.resolveTarget?.targetType)
    ? ["REMOVE_CONTENT", "WARN_USER", "LOCK_ACCOUNT", "DISMISSED"]
    : ["WARN_USER", "LOCK_ACCOUNT", "DISMISSED"];
  return <div>
    <PageHeader title="Báo cáo" subtitle="Xem hàng đợi và thực thi quyết định xử lý báo cáo."
      actions={<select value={page.statusFilter} onChange={(e) => page.setStatusFilter(e.target.value)}>
        <option value="">Tất cả</option><option value="PENDING">PENDING</option>
        <option value="RESOLVED">RESOLVED</option><option value="DISMISSED">DISMISSED</option>
      </select>} />
    {page.loading ? <p>Đang tải hàng đợi báo cáo...</p> : page.reports.length === 0 ?
      <EmptyState icon="📭" message="Không có báo cáo nào." /> : <Table>
        <Table.Head><Table.Row><Table.HeaderCell>Người báo cáo</Table.HeaderCell>
          <Table.HeaderCell>Đối tượng</Table.HeaderCell><Table.HeaderCell>ID</Table.HeaderCell>
          <Table.HeaderCell>Lý do</Table.HeaderCell><Table.HeaderCell>Trạng thái</Table.HeaderCell>
          <Table.HeaderCell>Ngày gửi</Table.HeaderCell><Table.HeaderCell /></Table.Row></Table.Head>
        <Table.Body>{page.reports.map((report) => {
          const badge = STATUS_BADGE[report.status] ?? STATUS_BADGE.PENDING;
          return <Table.Row key={report.id}><Table.Cell>{report.reporterEmail || "Hệ thống"}</Table.Cell>
            <Table.Cell>{report.targetType}</Table.Cell><Table.Cell>#{report.targetId}</Table.Cell>
            <Table.Cell className="ui-table__truncate">{report.reason || "—"}</Table.Cell>
            <Table.Cell><Badge intent={badge.intent}>{badge.label}</Badge></Table.Cell>
            <Table.Cell>{formatDate(report.createdAt)}</Table.Cell><Table.Cell>
              {report.status === "PENDING" && <Button variant="secondary" onClick={() => page.openResolveModal(report)}>Xử lý</Button>}
            </Table.Cell></Table.Row>;
        })}</Table.Body>
      </Table>}
    <Modal open={!!page.resolveTarget} onClose={page.closeResolveModal} title="Xử lý báo cáo">
      <p>{page.resolveTarget?.targetType} #{page.resolveTarget?.targetId}</p>
      <label style={{ display: "grid", gap: 6, marginBottom: 16 }}>Hành động
        <select value={page.actionTaken} onChange={(e) => page.setActionTaken(e.target.value)}>
          <option value="">Chọn hành động...</option>
          {actions.map((action) => <option key={action} value={action}>{ACTION_LABELS[action]}</option>)}
        </select></label>
      <label style={{ display: "grid", gap: 6 }}>Phản hồi Admin (tùy chọn)
        <textarea rows={4} value={page.adminResponse} onChange={(e) => page.setAdminResponse(e.target.value)} />
      </label>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 20 }}>
        <Button variant="secondary" onClick={page.closeResolveModal} disabled={page.submitting}>Hủy</Button>
        <Button onClick={page.submitResolve} disabled={page.submitting || !page.actionTaken}>
          {page.submitting ? "Đang xử lý..." : "Xác nhận"}</Button>
      </div>
    </Modal>
  </div>;
}
