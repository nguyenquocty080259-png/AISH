import { useState } from "react";
import Button from "../../components/ui/Button";
import Card from "../../components/ui/Card";
import Badge from "../../components/ui/Badge";
import EmptyState from "../../components/ui/EmptyState";
import PageHeader from "../../components/ui/PageHeader";
import Modal from "../../components/ui/Modal";
import "./ui-preview.css";

// TEMPORARY internal preview for the shared ui/ primitives — remove once pages adopt them
// (or once design review is done). Not linked from the sidebar; reach it directly at /_ui-preview.
export default function UiPreviewPage() {
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <div className="ui-preview">
      <PageHeader
        title="UI Primitives Preview"
        subtitle="Temporary route — not linked in nav, safe to delete later."
      />

      <section className="ui-preview__section">
        <h2>Button</h2>
        <div className="ui-preview__row">
          <Button variant="primary">Primary</Button>
          <Button variant="secondary">Secondary</Button>
          <Button variant="ghost">Ghost</Button>
          <Button variant="danger">Danger</Button>
          <Button variant="primary" disabled>Disabled</Button>
        </div>
      </section>

      <section className="ui-preview__section">
        <h2>Card</h2>
        <div className="ui-preview__row">
          <Card>Static card</Card>
          <Card clickable>Clickable card (hover me)</Card>
        </div>
      </section>

      <section className="ui-preview__section">
        <h2>Badge</h2>
        <div className="ui-preview__row">
          <Badge intent="neutral">NEUTRAL</Badge>
          <Badge intent="success">APPROVED</Badge>
          <Badge intent="warning">PENDING</Badge>
          <Badge intent="error">REJECTED</Badge>
          <Badge intent="info">CLOUD</Badge>
        </div>
      </section>

      <section className="ui-preview__section">
        <h2>EmptyState</h2>
        <div className="ui-preview__row">
          <Card className="ui-preview__empty-demo">
            <EmptyState icon="📭" message="Chưa có collection nào." />
          </Card>
          <Card className="ui-preview__empty-demo">
            <EmptyState
              icon="🗂️"
              message="Chưa có tài liệu nào."
              actionLabel="Tải lên tài liệu"
              onAction={() => alert("Action clicked")}
            />
          </Card>
        </div>
      </section>

      <section className="ui-preview__section">
        <h2>PageHeader</h2>
        <Card>
          <PageHeader
            title="Ví dụ tiêu đề trang"
            subtitle="Mô tả phụ tuỳ chọn"
            actions={<Button variant="primary">+ Hành động</Button>}
          />
        </Card>
      </section>

      <section className="ui-preview__section">
        <h2>Modal</h2>
        <Button variant="primary" onClick={() => setModalOpen(true)}>
          Mở modal
        </Button>
        <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Ví dụ modal">
          <p>Nhấn Esc hoặc bấm ra ngoài để đóng.</p>
          <div className="ui-preview__row" style={{ marginTop: "1rem" }}>
            <Button variant="secondary" onClick={() => setModalOpen(false)}>Hủy</Button>
            <Button variant="primary" onClick={() => setModalOpen(false)}>Xác nhận</Button>
          </div>
        </Modal>
      </section>
    </div>
  );
}
