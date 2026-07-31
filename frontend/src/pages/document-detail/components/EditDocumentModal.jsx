import { useState } from "react";
import { useTranslation } from "react-i18next";
import * as aiApi from "../../../api/aiApi";
import Modal from "../../../components/ui/Modal";
import Button from "../../../components/ui/Button";
import { Field, Input, Textarea } from "../../../components/ui/Field";

// Modal sửa metadata: title, description, subjects. Prefill từ doc hiện tại.
export default function EditDocumentModal({ open, doc, subjects, submitting, onClose, onSubmit }) {
  const { t } = useTranslation();
  const [title, setTitle] = useState(doc?.title ?? "");
  const [description, setDescription] = useState(doc?.description ?? "");
  const [subjectIds, setSubjectIds] = useState(doc?.subjectIds ?? []);
  const [subjectSearch, setSubjectSearch] = useState("");
  const [subjectError, setSubjectError] = useState(false);
  const [suggesting, setSuggesting] = useState(false);
  const [suggestionError, setSuggestionError] = useState("");

  const kw = subjectSearch.trim().toLowerCase();
  const filteredSubjects = kw
    ? subjects.filter((s) => s.name.toLowerCase().includes(kw))
    : subjects;

  const toggleSubject = (id) => {
    setSubjectError(false);
    setSubjectIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
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
    catch (error) { setSuggestionError(error.response?.data?.message || error.message || t("docDetail.editModal.aiSuggestError")); }
    finally { setSuggesting(false); }
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={t("docDetail.editModal.title")}
      size="lg"
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={submitting}>
            {t("common.actions.cancel")}
          </Button>
          <Button type="submit" form="edit-doc-form" loading={submitting}>
            {submitting ? t("docDetail.editModal.saving") : t("docDetail.editModal.save")}
          </Button>
        </>
      }
    >
      <form id="edit-doc-form" className="doc-form__form" onSubmit={handleSubmit}>
        <Input
          label={t("docDetail.editModal.name")}
          required
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />

        <div>
          <Button variant="secondary" size="sm" onClick={suggest} loading={suggesting} disabled={submitting}>
            {suggesting ? t("docDetail.editModal.suggesting") : t("docDetail.editModal.aiSuggest")}
          </Button>
          {suggestionError && <p className="doc-form__error">{suggestionError}</p>}
        </div>

        <Textarea
          label={t("docDetail.editModal.description")}
          rows={3}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <Field
          id="edit-doc-subjects"
          label={t("docDetail.editModal.subjectLabel")}
          error={subjectError ? t("docDetail.editModal.subjectRequired") : undefined}
        >
          {subjectIds.length > 0 && (
            <div className="doc-form__chips">
              {subjectIds.map((id) => {
                const s = subjects.find((x) => x.id === id);
                if (!s) return null;
                return (
                  <button
                    key={id}
                    type="button"
                    className="doc-form__chip has-custom-focus"
                    onClick={() => toggleSubject(id)}
                    title={t("docDetail.editModal.subjectRemoveHint")}
                  >
                    {s.name}
                    <span className="doc-form__chip-x" aria-hidden="true">×</span>
                  </button>
                );
              })}
            </div>
          )}

          <Input
            id="edit-doc-subjects"
            type="search"
            placeholder={t("docDetail.editModal.subjectSearch")}
            aria-label={t("docDetail.editModal.subjectSearch")}
            value={subjectSearch}
            onChange={(e) => setSubjectSearch(e.target.value)}
          />

          <div className="doc-form__subject-list">
            {filteredSubjects.length === 0 && (
              <p className="doc-form__subject-empty">{t("docDetail.editModal.noSubject")}</p>
            )}
            {filteredSubjects.map((s) => {
              const checked = subjectIds.includes(s.id);
              return (
                <button
                  key={s.id}
                  type="button"
                  role="checkbox"
                  aria-checked={checked}
                  onClick={() => toggleSubject(s.id)}
                  className={`doc-form__subject has-custom-focus ${
                    checked ? "doc-form__subject--checked" : ""
                  }`.trim()}
                >
                  <input type="checkbox" checked={checked} readOnly tabIndex={-1} aria-hidden="true" />
                  <span>{s.name}</span>
                </button>
              );
            })}
          </div>
        </Field>
      </form>
    </Modal>
  );
}
