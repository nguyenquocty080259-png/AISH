import { useEffect, useState } from "react";
import PageHeader from "../../../components/ui/PageHeader";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
import EmptyState from "../../../components/ui/EmptyState";
import Table from "../../../components/ui/Table";
import AdminPagination from "../components/AdminPagination";
import { useAdminUsersPage } from "./hooks/useAdminUsersPage";
import { useToast } from "../../../hooks/useToast";
import "./admin-users.css";

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("vi-VN");
}

export default function AdminUsersPage() {
  const pageState = useAdminUsersPage();
  const { showSuccess, showError } = useToast();
  const [createForm, setCreateForm] = useState({ fullName: "", email: "", password: "", role: "USER" });
  const [editForm, setEditForm] = useState({ fullName: "", avatarUrl: "", role: "USER" });
  const [newPassword, setNewPassword] = useState("");

  useEffect(() => {
    if (!pageState.editTarget) return;
    setEditForm({
      fullName: pageState.editTarget.fullName ?? "",
      avatarUrl: pageState.editTarget.avatarUrl ?? "",
      role: pageState.editTarget.role ?? "USER",
    });
  }, [pageState.editTarget]);

  useEffect(() => {
    if (pageState.resetTarget) setNewPassword("");
  }, [pageState.resetTarget]);

  const submitCreate = async (event) => {
    event.preventDefault();
    const created = await pageState.createUser({
      ...createForm,
      fullName: createForm.fullName.trim(),
      email: createForm.email.trim(),
    });
    if (created) setCreateForm({ fullName: "", email: "", password: "", role: "USER" });
  };

  const submitEdit = (event) => {
    event.preventDefault();
    pageState.updateUser({
      fullName: editForm.fullName.trim(),
      avatarUrl: editForm.avatarUrl.trim() || null,
      role: editForm.role,
    });
  };

  const submitReset = (event) => {
    event.preventDefault();
    pageState.resetPassword(newPassword);
  };

  const copySeedPassword = async (password) => {
    try {
      await navigator.clipboard.writeText(password);
      showSuccess("Đã sao chép mật khẩu seed.");
    } catch {
      showError("Không thể sao chép mật khẩu. Vui lòng copy thủ công.");
    }
  };

  return (
    <div className="admin-users-page">
      <PageHeader
        title="Quản lý người dùng"
        subtitle="Tạo tài khoản, phân quyền, khóa hoặc đặt lại mật khẩu người dùng."
        actions={<Button onClick={pageState.openCreateModal}>+ Tạo tài khoản</Button>}
      />

      <div className="admin-users-toolbar">
        <label className="admin-users-search">
          <span>Tìm người dùng</span>
          <input
            type="search"
            value={pageState.search}
            onChange={(event) => pageState.setSearch(event.target.value)}
            placeholder="Họ tên hoặc email"
          />
        </label>
        <span className="admin-users-count">{pageState.filteredUserCount} tài khoản</span>
      </div>

      {pageState.loading ? (
        <p className="admin-users-page__loading">Đang tải danh sách người dùng...</p>
      ) : pageState.visibleUsers.length === 0 ? (
        <EmptyState icon="👤" message="Không tìm thấy người dùng phù hợp." />
      ) : (
        <>
          <div className="admin-users-table-wrap">
            <Table>
              <Table.Head>
                <Table.Row>
                  <Table.HeaderCell>Họ tên</Table.HeaderCell>
                  <Table.HeaderCell>Email</Table.HeaderCell>
                  <Table.HeaderCell>Vai trò</Table.HeaderCell>
                  <Table.HeaderCell>Trạng thái</Table.HeaderCell>
                  <Table.HeaderCell>Mật khẩu (seed)</Table.HeaderCell>
                  <Table.HeaderCell>Đăng nhập gần nhất</Table.HeaderCell>
                  <Table.HeaderCell />
                </Table.Row>
              </Table.Head>
              <Table.Body>
                {pageState.visibleUsers.map((user) => (
                  <Table.Row key={user.id}>
                    <Table.Cell>{user.fullName}</Table.Cell>
                    <Table.Cell>{user.email}</Table.Cell>
                    <Table.Cell>
                      <span className={`admin-users-badge admin-users-badge--${user.role.toLowerCase()}`}>
                        {user.role}
                      </span>
                    </Table.Cell>
                    <Table.Cell>
                      <span className={`admin-users-status admin-users-status--${user.status.toLowerCase()}`}>
                        {user.status}
                      </span>
                    </Table.Cell>
                    <Table.Cell>
                      {user.seedPassword ? (
                        <div className="admin-users-seed-password">
                          <code>{user.seedPassword}</code>
                          <button
                            type="button"
                            className="admin-users-copy"
                            onClick={() => copySeedPassword(user.seedPassword)}
                            aria-label={`Sao chép mật khẩu của ${user.email}`}
                          >
                            Copy
                          </button>
                        </div>
                      ) : "—"}
                    </Table.Cell>
                    <Table.Cell>{formatDate(user.lastLoginAt)}</Table.Cell>
                    <Table.Cell>
                      <div className="admin-users-actions">
                        <Button variant="secondary" onClick={() => pageState.openEditModal(user)}>Sửa</Button>
                        {!user.seedPassword && (
                          <Button variant="secondary" onClick={() => pageState.openResetModal(user)}>
                            Đặt lại mật khẩu
                          </Button>
                        )}
                        <Button
                          variant={user.status === "BANNED" ? "secondary" : "danger"}
                          disabled={pageState.statusUpdatingId === user.id}
                          onClick={() => pageState.toggleUserStatus(user)}
                        >
                          {user.status === "BANNED" ? "Mở khóa" : "Khóa"}
                        </Button>
                      </div>
                    </Table.Cell>
                  </Table.Row>
                ))}
              </Table.Body>
            </Table>
          </div>
          <AdminPagination
            page={pageState.page}
            totalPages={pageState.totalPages}
            onPrev={() => pageState.setPage((page) => page - 1)}
            onNext={() => pageState.setPage((page) => page + 1)}
          />
        </>
      )}

      <Modal open={pageState.createModalOpen} onClose={pageState.closeCreateModal} title="Tạo tài khoản">
        <form className="admin-users-form" onSubmit={submitCreate}>
          <label>Họ tên<input required autoFocus value={createForm.fullName} onChange={(e) => setCreateForm({ ...createForm, fullName: e.target.value })} /></label>
          <label>Email<input required type="email" value={createForm.email} onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })} /></label>
          <label>Mật khẩu<input required type="password" minLength={8} value={createForm.password} onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })} /></label>
          <label>Vai trò<select value={createForm.role} onChange={(e) => setCreateForm({ ...createForm, role: e.target.value })}><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select></label>
          <div className="admin-users-form__actions"><Button variant="secondary" onClick={pageState.closeCreateModal} disabled={pageState.creating}>Hủy</Button><Button type="submit" disabled={pageState.creating}>{pageState.creating ? "Đang tạo..." : "Tạo"}</Button></div>
        </form>
      </Modal>

      <Modal open={!!pageState.editTarget} onClose={pageState.closeEditModal} title="Sửa tài khoản">
        <form className="admin-users-form" onSubmit={submitEdit}>
          <label>Họ tên<input required autoFocus value={editForm.fullName} onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })} /></label>
          <label>Avatar URL<input type="url" value={editForm.avatarUrl} onChange={(e) => setEditForm({ ...editForm, avatarUrl: e.target.value })} /></label>
          <label>Vai trò<select value={editForm.role} onChange={(e) => setEditForm({ ...editForm, role: e.target.value })}><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select></label>
          <div className="admin-users-form__actions"><Button variant="secondary" onClick={pageState.closeEditModal} disabled={pageState.updating}>Hủy</Button><Button type="submit" disabled={pageState.updating}>{pageState.updating ? "Đang lưu..." : "Lưu"}</Button></div>
        </form>
      </Modal>

      <Modal open={!!pageState.resetTarget} onClose={pageState.closeResetModal} title="Đặt lại mật khẩu">
        <form className="admin-users-form" onSubmit={submitReset}>
          <p className="admin-users-form__hint">Đặt mật khẩu mới cho {pageState.resetTarget?.fullName} ({pageState.resetTarget?.email}).</p>
          <label>Mật khẩu mới<input required autoFocus type="password" minLength={6} value={newPassword} onChange={(e) => setNewPassword(e.target.value)} /></label>
          <div className="admin-users-form__actions"><Button variant="secondary" onClick={pageState.closeResetModal} disabled={pageState.resetting}>Hủy</Button><Button type="submit" disabled={pageState.resetting}>{pageState.resetting ? "Đang đặt lại..." : "Đặt lại"}</Button></div>
        </form>
      </Modal>
    </div>
  );
}
