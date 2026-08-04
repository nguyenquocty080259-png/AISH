import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PageHeader from "../../../components/ui/PageHeader";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
import EmptyState from "../../../components/ui/EmptyState";
import Table from "../../../components/ui/Table";
import { useAdminSubjectsPage } from "./hooks/useAdminSubjectsPage";
import "./admin-subjects.css";

// Trang Admin quản lý MÔN HỌC: bảng danh sách, thêm/đổi tên/xoá qua modal.
export default function AdminSubjectsPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (iso) => (iso ? new Date(iso).toLocaleString(locale) : "—");
  const {
    subjects,
    loading,
    createModalOpen,
    creating,
    openCreateModal,
    closeCreateModal,
    createSubject,
    renameTarget,
    renaming,
    openRenameModal,
    closeRenameModal,
    renameSubject,
    deleteTarget,
    deleting,
    openDeleteModal,
    closeDeleteModal,
    deleteSubject,
  } = useAdminSubjectsPage();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const [renameName, setRenameName] = useState("");
  const [renameDescription, setRenameDescription] = useState("");

  useEffect(() => {
    if (renameTarget) {
      setRenameName(renameTarget.name ?? "");
      setRenameDescription(renameTarget.description ?? "");
    }
  }, [renameTarget]);

  const submitCreate = (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    createSubject({ name: name.trim(), description: description.trim() || undefined });
    setName("");
    setDescription("");
  };

  const submitRename = (e) => {
    e.preventDefault();
    if (!renameName.trim()) return;
    renameSubject({ name: renameName.trim(), description: renameDescription.trim() || undefined });
  };

  return (
    <div className="admin-subjects-page">
      <PageHeader
        title={t("admin.subjects.title")}
        subtitle={t("admin.subjects.subtitle")}
        actions={
          <Button variant="primary" onClick={openCreateModal}>
            {t("admin.subjects.addBtn")}
          </Button>
        }
      />

      {loading ? (
        <p className="admin-subjects-page__loading">{t("admin.subjects.loading")}</p>
      ) : subjects.length === 0 ? (
        <EmptyState
          icon="📚"
          message={t("admin.subjects.empty")}
          actionLabel={t("admin.subjects.emptyAction")}
          onAction={openCreateModal}
        />
      ) : (
        <Table>
          <Table.Head>
            <Table.Row>
              <Table.HeaderCell>{t("admin.subjects.colName")}</Table.HeaderCell>
              <Table.HeaderCell>{t("admin.subjects.colDesc")}</Table.HeaderCell>
              <Table.HeaderCell>{t("admin.subjects.colCreated")}</Table.HeaderCell>
              <Table.HeaderCell />
            </Table.Row>
          </Table.Head>
          <Table.Body>
            {subjects.map((subject) => (
              <Table.Row key={subject.id}>
                <Table.Cell>{subject.name}</Table.Cell>
                <Table.Cell className="ui-table__truncate">
                  {subject.description || "—"}
                </Table.Cell>
                <Table.Cell>{formatDate(subject.createdAt)}</Table.Cell>
                <Table.Cell>
                  <div className="ui-table__actions">
                    <Button variant="secondary" onClick={() => openRenameModal(subject)}>
                      {t("admin.subjects.rename")}
                    </Button>
                    <Button variant="danger" onClick={() => openDeleteModal(subject)}>
                      {t("common.actions.delete")}
                    </Button>
                  </div>
                </Table.Cell>
              </Table.Row>
            ))}
          </Table.Body>
        </Table>
      )}

      <Modal open={createModalOpen} onClose={closeCreateModal} title={t("admin.subjects.addTitle")}>
        <form className="admin-subjects-form" onSubmit={submitCreate}>
          <label className="admin-subjects-form__field">
            {t("admin.subjects.nameLabel")}
            <input type="text" autoFocus value={name} onChange={(e) => setName(e.target.value)} />
          </label>
          <label className="admin-subjects-form__field">
            {t("admin.subjects.descLabel")}
            <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
          </label>
          <div className="admin-subjects-form__actions">
            <Button type="button" variant="secondary" onClick={closeCreateModal} disabled={creating}>
              {t("common.actions.cancel")}
            </Button>
            <Button type="submit" variant="primary" disabled={creating}>
              {creating ? t("admin.common.adding") : t("admin.subjects.add")}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!renameTarget} onClose={closeRenameModal} title={t("admin.subjects.renameTitle")}>
        <form className="admin-subjects-form" onSubmit={submitRename}>
          <label className="admin-subjects-form__field">
            {t("admin.subjects.nameLabel")}
            <input
              type="text"
              autoFocus
              value={renameName}
              onChange={(e) => setRenameName(e.target.value)}
            />
          </label>
          <label className="admin-subjects-form__field">
            {t("admin.subjects.descLabel")}
            <textarea
              rows={3}
              value={renameDescription}
              onChange={(e) => setRenameDescription(e.target.value)}
            />
          </label>
          <div className="admin-subjects-form__actions">
            <Button type="button" variant="secondary" onClick={closeRenameModal} disabled={renaming}>
              {t("common.actions.cancel")}
            </Button>
            <Button type="submit" variant="primary" disabled={renaming}>
              {renaming ? t("admin.common.saving") : t("common.actions.save")}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!deleteTarget} onClose={closeDeleteModal} title={t("admin.subjects.deleteTitle")}>
        <p className="admin-subjects-page__confirm-text">
          {t("admin.subjects.deleteConfirm", { name: deleteTarget?.name })}
        </p>
        <div className="admin-subjects-form__actions">
          <Button variant="secondary" onClick={closeDeleteModal} disabled={deleting}>
            {t("common.actions.cancel")}
          </Button>
          <Button variant="danger" onClick={deleteSubject} disabled={deleting}>
            {deleting ? t("admin.common.deleting") : t("common.actions.delete")}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
