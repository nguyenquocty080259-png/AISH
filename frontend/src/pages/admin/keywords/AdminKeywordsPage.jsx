import { useEffect, useState } from "react";
import Button from "../../../components/ui/Button";
import EmptyState from "../../../components/ui/EmptyState";
import Modal from "../../../components/ui/Modal";
import PageHeader from "../../../components/ui/PageHeader";
import Table from "../../../components/ui/Table";
import { KEYWORD_TYPES, useAdminKeywordsPage } from "./hooks/useAdminKeywordsPage";
import "./admin-keywords.css";

function formatDate(value) {
  return value ? new Date(value).toLocaleString("vi-VN") : "—";
}

export default function AdminKeywordsPage() {
  const {
    activeType,
    setActiveType,
    keywords,
    loading,
    creating,
    pendingIds,
    createKeyword,
    toggleActive,
    editTarget,
    setEditTarget,
    saveEdit,
    deleteTarget,
    setDeleteTarget,
    deleteKeyword,
  } = useAdminKeywordsPage();
  const [newKeyword, setNewKeyword] = useState("");
  const [editedKeyword, setEditedKeyword] = useState("");

  useEffect(() => {
    setEditedKeyword(editTarget?.keyword ?? "");
  }, [editTarget]);

  const submitCreate = async (event) => {
    event.preventDefault();
    const keyword = newKeyword.trim();
    if (!keyword) return;
    if (await createKeyword(keyword)) setNewKeyword("");
  };

  const submitEdit = (event) => {
    event.preventDefault();
    const keyword = editedKeyword.trim();
    if (keyword) saveEdit(keyword);
  };

  const deletePending = deleteTarget ? pendingIds.has(deleteTarget.id) : false;
  const editPending = editTarget ? pendingIds.has(editTarget.id) : false;

  return (
    <div className="admin-keywords-page">
      <PageHeader
        title="Quản lý từ khóa kiểm duyệt"
        subtitle="Cấu hình từ khóa theo từng khu vực. Hiện tại bộ lọc tự động chỉ áp dụng cho AI Chat."
      />

      <div className="admin-keywords-tabs" role="tablist" aria-label="Loại từ khóa">
        {KEYWORD_TYPES.map((type) => (
          <button
            key={type.value}
            type="button"
            role="tab"
            aria-selected={activeType === type.value}
            className={`admin-keywords-tabs__item${activeType === type.value ? " admin-keywords-tabs__item--active" : ""}`}
            onClick={() => setActiveType(type.value)}
          >
            {type.label}
          </button>
        ))}
      </div>

      <form className="admin-keywords-add" onSubmit={submitCreate}>
        <label htmlFor="new-moderation-keyword">Thêm từ khóa</label>
        <div className="admin-keywords-add__controls">
          <input
            id="new-moderation-keyword"
            type="text"
            maxLength={255}
            placeholder="Nhập từ khóa cần kiểm duyệt"
            value={newKeyword}
            onChange={(event) => setNewKeyword(event.target.value)}
          />
          <Button type="submit" variant="primary" disabled={creating || !newKeyword.trim()}>
            {creating ? "Đang thêm..." : "Thêm"}
          </Button>
        </div>
      </form>

      {loading ? (
        <p className="admin-keywords-page__loading">Đang tải danh sách từ khóa...</p>
      ) : keywords.length === 0 ? (
        <EmptyState icon="⌕" message="Chưa có từ khóa nào trong loại này." />
      ) : (
        <Table>
          <Table.Head>
            <Table.Row>
              <Table.HeaderCell>Từ khóa</Table.HeaderCell>
              <Table.HeaderCell>Trạng thái</Table.HeaderCell>
              <Table.HeaderCell>Ngày tạo</Table.HeaderCell>
              <Table.HeaderCell />
            </Table.Row>
          </Table.Head>
          <Table.Body>
            {keywords.map((item) => {
              const pending = pendingIds.has(item.id);
              return (
                <Table.Row key={item.id}>
                  <Table.Cell>{item.keyword}</Table.Cell>
                  <Table.Cell>
                    <label className="admin-keywords-toggle">
                      <input
                        type="checkbox"
                        checked={item.active}
                        disabled={pending}
                        onChange={() => toggleActive(item)}
                      />
                      <span>{pending ? "Đang lưu..." : item.active ? "Đang bật" : "Đã tắt"}</span>
                    </label>
                  </Table.Cell>
                  <Table.Cell>{formatDate(item.createdAt)}</Table.Cell>
                  <Table.Cell>
                    <div className="ui-table__actions">
                      <Button variant="secondary" disabled={pending} onClick={() => setEditTarget(item)}>
                        Sửa
                      </Button>
                      <Button variant="danger" disabled={pending} onClick={() => setDeleteTarget(item)}>
                        Xóa
                      </Button>
                    </div>
                  </Table.Cell>
                </Table.Row>
              );
            })}
          </Table.Body>
        </Table>
      )}

      <Modal open={!!editTarget} onClose={() => setEditTarget(null)} title="Sửa từ khóa">
        <form className="admin-keywords-form" onSubmit={submitEdit}>
          <label className="admin-keywords-form__field">
            Từ khóa
            <input
              type="text"
              autoFocus
              maxLength={255}
              value={editedKeyword}
              onChange={(event) => setEditedKeyword(event.target.value)}
            />
          </label>
          <div className="admin-keywords-form__actions">
            <Button type="button" variant="secondary" disabled={editPending} onClick={() => setEditTarget(null)}>
              Hủy
            </Button>
            <Button type="submit" variant="primary" disabled={editPending || !editedKeyword.trim()}>
              {editPending ? "Đang lưu..." : "Lưu"}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!deleteTarget} onClose={() => setDeleteTarget(null)} title="Xóa từ khóa">
        <p className="admin-keywords-page__confirm-text">
          Bạn có chắc muốn xóa từ khóa “{deleteTarget?.keyword}” không?
        </p>
        <div className="admin-keywords-form__actions">
          <Button variant="secondary" disabled={deletePending} onClick={() => setDeleteTarget(null)}>
            Hủy
          </Button>
          <Button variant="danger" disabled={deletePending} onClick={deleteKeyword}>
            {deletePending ? "Đang xóa..." : "Xóa"}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
