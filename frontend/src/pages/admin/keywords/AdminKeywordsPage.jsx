import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import Button from "../../../components/ui/Button";
import EmptyState from "../../../components/ui/EmptyState";
import Modal from "../../../components/ui/Modal";
import PageHeader from "../../../components/ui/PageHeader";
import Table from "../../../components/ui/Table";
import { KEYWORD_TYPES, useAdminKeywordsPage } from "./hooks/useAdminKeywordsPage";
import "./admin-keywords.css";

// Trang Admin quản lý TỪ KHOÁ CẤM: chọn loại qua tab, thêm/tìm/lọc/bật-tắt/sửa/xoá từng từ khoá.
export default function AdminKeywordsPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (value) => (value ? new Date(value).toLocaleString(locale) : "—");
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
  const [search, setSearch] = useState("");
  const [activeFilter, setActiveFilter] = useState("all");

  const filteredKeywords = useMemo(() => {
    const query = search.trim().toLowerCase();
    return keywords.filter((item) => {
      const matchesSearch = !query || item.keyword.toLowerCase().includes(query);
      const matchesActive = activeFilter === "all"
        || (activeFilter === "active" && item.active)
        || (activeFilter === "inactive" && !item.active);
      return matchesSearch && matchesActive;
    });
  }, [keywords, search, activeFilter]);

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
        title={t("admin.keywords.title")}
        subtitle={t("admin.keywords.subtitle")}
      />

      <div className="admin-keywords-tabs" role="tablist" aria-label={t("admin.keywords.tabsAria")}>
        {KEYWORD_TYPES.map((type) => (
          <button
            key={type.value}
            type="button"
            role="tab"
            aria-selected={activeType === type.value}
            className={`admin-keywords-tabs__item${activeType === type.value ? " admin-keywords-tabs__item--active" : ""}`}
            onClick={() => setActiveType(type.value)}
          >
            {t(type.labelKey)}
          </button>
        ))}
      </div>

      <form className="admin-keywords-add" onSubmit={submitCreate}>
        <label htmlFor="new-moderation-keyword">{t("admin.keywords.addLabel")}</label>
        <div className="admin-keywords-add__controls">
          <input
            id="new-moderation-keyword"
            type="text"
            maxLength={255}
            placeholder={t("admin.keywords.addPlaceholder")}
            value={newKeyword}
            onChange={(event) => setNewKeyword(event.target.value)}
          />
          <Button type="submit" variant="primary" disabled={creating || !newKeyword.trim()}>
            {creating ? t("admin.common.adding") : t("admin.keywords.add")}
          </Button>
        </div>
      </form>

      <div className="admin-keywords-filters">
        <label>
          {t("admin.keywords.searchLabel")}
          <input
            type="search"
            placeholder={t("admin.keywords.searchPlaceholder")}
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </label>
        <label>
          {t("admin.keywords.statusLabel")}
          <select value={activeFilter} onChange={(event) => setActiveFilter(event.target.value)}>
            <option value="all">{t("admin.keywords.statusAll")}</option>
            <option value="active">{t("admin.keywords.statusActive")}</option>
            <option value="inactive">{t("admin.keywords.statusInactive")}</option>
          </select>
        </label>
      </div>

      {loading ? (
        <p className="admin-keywords-page__loading">{t("admin.keywords.loading")}</p>
      ) : filteredKeywords.length === 0 ? (
        <EmptyState icon="⌕" message={keywords.length === 0
          ? t("admin.keywords.emptyNone")
          : t("admin.keywords.emptyFilter")} />
      ) : (
        <Table>
          <Table.Head>
            <Table.Row>
              <Table.HeaderCell>{t("admin.keywords.colKeyword")}</Table.HeaderCell>
              <Table.HeaderCell>{t("admin.keywords.colStatus")}</Table.HeaderCell>
              <Table.HeaderCell>{t("admin.keywords.colCreated")}</Table.HeaderCell>
              <Table.HeaderCell />
            </Table.Row>
          </Table.Head>
          <Table.Body>
            {filteredKeywords.map((item) => {
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
                      <span>{pending ? t("admin.common.saving") : item.active ? t("admin.keywords.statusActive") : t("admin.keywords.statusInactive")}</span>
                    </label>
                  </Table.Cell>
                  <Table.Cell>{formatDate(item.createdAt)}</Table.Cell>
                  <Table.Cell>
                    <div className="ui-table__actions">
                      <Button variant="secondary" disabled={pending} onClick={() => setEditTarget(item)}>
                        {t("common.actions.edit")}
                      </Button>
                      <Button variant="danger" disabled={pending} onClick={() => setDeleteTarget(item)}>
                        {t("common.actions.delete")}
                      </Button>
                    </div>
                  </Table.Cell>
                </Table.Row>
              );
            })}
          </Table.Body>
        </Table>
      )}

      <Modal open={!!editTarget} onClose={() => setEditTarget(null)} title={t("admin.keywords.editTitle")}>
        <form className="admin-keywords-form" onSubmit={submitEdit}>
          <label className="admin-keywords-form__field">
            {t("admin.keywords.keywordLabel")}
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
              {t("common.actions.cancel")}
            </Button>
            <Button type="submit" variant="primary" disabled={editPending || !editedKeyword.trim()}>
              {editPending ? t("admin.common.saving") : t("common.actions.save")}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!deleteTarget} onClose={() => setDeleteTarget(null)} title={t("admin.keywords.deleteTitle")}>
        <p className="admin-keywords-page__confirm-text">
          {t("admin.keywords.deleteConfirm", { keyword: deleteTarget?.keyword })}
        </p>
        <div className="admin-keywords-form__actions">
          <Button variant="secondary" disabled={deletePending} onClick={() => setDeleteTarget(null)}>
            {t("common.actions.cancel")}
          </Button>
          <Button variant="danger" disabled={deletePending} onClick={deleteKeyword}>
            {deletePending ? t("admin.common.deleting") : t("common.actions.delete")}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
