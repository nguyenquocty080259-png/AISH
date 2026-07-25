import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import Button from "../../../../components/ui/Button";
import Modal from "../../../../components/ui/Modal";
import * as aiApi from "../../../../api/aiApi";

// Modal Admin sửa metadata tài liệu (title/description/subjectIds) — field shape đồng bộ với
// document-detail/hooks/useDocumentDetailPage.js (handleUpdateDocument).
export default function EditDocumentModal({ open, doc, subjects, submitting, loading, onClose, onSubmit }) {
  const { t } = useTranslation();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [subjectIds, setSubjectIds] = useState([]);
  const [subjectError, setSubjectError] = useState(false);
  const [suggesting, setSuggesting] = useState(false);
  const [suggestionError, setSuggestionError] = useState("");

  useEffect(() => {
    if (doc) {
      setTitle(doc.title ?? "");
      setDescription(doc.description ?? "");
      setSubjectIds(doc.subjectIds ?? []);
      setSubjectError(false);
    }
  }, [doc]);

  const toggleSubject = (id) => {
    setSubjectError(false);
    setSubjectIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (subjectIds.length === 0) {
      setSubjectError(true);
      return;
    }
    onSubmit({ title, description, subjectIds });
  };
  const suggest = async () => {
    setSuggesting(true); setSuggestionError("");
    try { const data = await aiApi.suggestMetadata(doc.id); setTitle(data.title ?? ""); setDescription(data.description ?? ""); setSubjectIds(data.subjectIds ?? []); }
    catch (error) { setSuggestionError(error.response?.data?.message || error.message || t("admin.documents.aiSuggestError")); }
    finally { setSuggesting(false); }
  };

  return (
    <Modal open={open} onClose={onClose} title={t("admin.documents.editTitle")}>
      {loading ? (
        <p className="admin-documents-page__loading">{t("admin.documents.editLoading")}</p>
      ) : (
      <form className="admin-subjects-form" onSubmit={handleSubmit}>
        <label className="admin-subjects-form__field">
          {t("admin.documents.editName")}
          <input type="text" required value={title} onChange={(e) => setTitle(e.target.value)} />
        </label>
        <Button type="button" variant="secondary" onClick={suggest} disabled={suggesting || submitting}>
          {suggesting ? t("admin.documents.suggesting") : t("admin.documents.aiSuggest")}
        </Button>
        {suggestionError && <p className="admin-documents-page__subject-error">{suggestionError}</p>}

        <label className="admin-subjects-form__field">
          {t("admin.documents.editDesc")}
          <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>

        <div className="admin-subjects-form__field">
          {t("admin.documents.editSubjectLabel")}
          <div className="admin-documents-page__subject-list">
            {subjects.map((s) => {
              const checked = subjectIds.includes(s.id);
              return (
                <label key={s.id} className="admin-documents-page__subject-item">
                  <input type="checkbox" checked={checked} onChange={() => toggleSubject(s.id)} />
                  {s.name}
                </label>
              );
            })}
          </div>
          {subjectError && (
            <p className="admin-documents-page__subject-error">{t("admin.documents.subjectRequired")}</p>
          )}
        </div>

        <div className="admin-subjects-form__actions">
          <Button type="button" variant="secondary" onClick={onClose} disabled={submitting}>
            {t("common.actions.cancel")}
          </Button>
          <Button type="submit" variant="primary" disabled={submitting}>
            {submitting ? t("admin.common.saving") : t("common.actions.save")}
          </Button>
        </div>
      </form>
      )}
    </Modal>
  );
}
