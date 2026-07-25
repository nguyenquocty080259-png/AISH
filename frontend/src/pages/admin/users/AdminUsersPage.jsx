import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PageHeader from "../../../components/ui/PageHeader";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
import EmptyState from "../../../components/ui/EmptyState";
import Table from "../../../components/ui/Table";
import AdminPagination from "../components/AdminPagination";
import { useAdminUsersPage } from "./hooks/useAdminUsersPage";
import { useToast } from "../../../hooks/useToast";
import "./admin-users.css";

export default function AdminUsersPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (iso) => (iso ? new Date(iso).toLocaleString(locale) : "—");
  const pageState = useAdminUsersPage();
  const { showSuccess, showError } = useToast();
  const [createForm, setCreateForm] = useState({ fullName: "", email: "", password: "", role: "USER" });
  const [role, setRole] = useState("USER");

  useEffect(() => {
    if (!pageState.editTarget) return;
    setRole(pageState.editTarget.role ?? "USER");
  }, [pageState.editTarget]);

  const submitCreate = async (event) => {
    event.preventDefault();
    const created = await pageState.createUser({
      ...createForm,
      fullName: createForm.fullName.trim(),
      email: createForm.email.trim(),
    });
    if (created) setCreateForm({ fullName: "", email: "", password: "", role: "USER" });
  };

  const submitRoleChange = (event) => {
    event.preventDefault();
    pageState.updateUserRole(role);
  };

  const copySeedPassword = async (password) => {
    try {
      await navigator.clipboard.writeText(password);
      showSuccess(t("admin.users.pwCopied"));
    } catch {
      showError(t("admin.users.pwCopyError"));
    }
  };

  return (
    <div className="admin-users-page">
      <PageHeader
        title={t("admin.users.title")}
        subtitle={t("admin.users.subtitle")}
        actions={<Button onClick={pageState.openCreateModal}>{t("admin.users.createBtn")}</Button>}
      />

      <div className="admin-users-toolbar">
        <label className="admin-users-search">
          <span>{t("admin.users.searchLabel")}</span>
          <input
            type="search"
            value={pageState.search}
            onChange={(event) => pageState.setSearch(event.target.value)}
            placeholder={t("admin.users.searchPlaceholder")}
          />
        </label>
        <span className="admin-users-count">{t("admin.users.countLabel", { count: pageState.filteredUserCount })}</span>
      </div>

      {pageState.loading ? (
        <p className="admin-users-page__loading">{t("admin.users.loading")}</p>
      ) : pageState.visibleUsers.length === 0 ? (
        <EmptyState icon="👤" message={t("admin.users.empty")} />
      ) : (
        <>
          <div className="admin-users-table-wrap">
            <Table>
              <Table.Head>
                <Table.Row>
                  <Table.HeaderCell>{t("admin.users.colFullName")}</Table.HeaderCell>
                  <Table.HeaderCell>{t("admin.users.colEmail")}</Table.HeaderCell>
                  <Table.HeaderCell>{t("admin.users.colRole")}</Table.HeaderCell>
                  <Table.HeaderCell>{t("admin.users.colStatus")}</Table.HeaderCell>
                  <Table.HeaderCell>{t("admin.users.colSeedPassword")}</Table.HeaderCell>
                  <Table.HeaderCell>{t("admin.users.colLastLogin")}</Table.HeaderCell>
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
                            aria-label={t("admin.users.copyPwAria", { email: user.email })}
                          >
                            {t("admin.users.copy")}
                          </button>
                        </div>
                      ) : "—"}
                    </Table.Cell>
                    <Table.Cell>{formatDate(user.lastLoginAt)}</Table.Cell>
                    <Table.Cell>
                      <div className="admin-users-actions">
                        <Button variant="secondary" onClick={() => pageState.openEditModal(user)}>{t("admin.users.changeRole")}</Button>
                        <Button
                          variant={user.status === "BANNED" ? "secondary" : "danger"}
                          disabled={pageState.statusUpdatingId === user.id}
                          onClick={() => pageState.toggleUserStatus(user)}
                        >
                          {user.status === "BANNED" ? t("admin.users.unlock") : t("admin.users.lock")}
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

      <Modal open={pageState.createModalOpen} onClose={pageState.closeCreateModal} title={t("admin.users.createTitle")}>
        <form className="admin-users-form" onSubmit={submitCreate}>
          <label>{t("admin.users.fullName")}<input required autoFocus value={createForm.fullName} onChange={(e) => setCreateForm({ ...createForm, fullName: e.target.value })} /></label>
          <label>{t("admin.users.email")}<input required type="email" value={createForm.email} onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })} /></label>
          <label>{t("admin.users.password")}<input required type="password" minLength={8} value={createForm.password} onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })} /></label>
          <label>{t("admin.users.role")}<select value={createForm.role} onChange={(e) => setCreateForm({ ...createForm, role: e.target.value })}><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select></label>
          <div className="admin-users-form__actions"><Button variant="secondary" onClick={pageState.closeCreateModal} disabled={pageState.creating}>{t("common.actions.cancel")}</Button><Button type="submit" disabled={pageState.creating}>{pageState.creating ? t("admin.common.creating") : t("admin.subjects.add")}</Button></div>
        </form>
      </Modal>

      <Modal open={!!pageState.editTarget} onClose={pageState.closeEditModal} title={t("admin.users.changeRoleTitle")}>
        <form className="admin-users-form" onSubmit={submitRoleChange}>
          <p className="admin-users-form__hint">
            {t("admin.users.changeRoleHint", { name: pageState.editTarget?.fullName, email: pageState.editTarget?.email })}
          </p>
          <label>{t("admin.users.role")}<select autoFocus value={role} onChange={(e) => setRole(e.target.value)}><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select></label>
          <div className="admin-users-form__actions"><Button variant="secondary" onClick={pageState.closeEditModal} disabled={pageState.updating}>{t("common.actions.cancel")}</Button><Button type="submit" disabled={pageState.updating}>{pageState.updating ? t("admin.common.saving") : t("common.actions.save")}</Button></div>
        </form>
      </Modal>
    </div>
  );
}
