import { useState } from "react";
import { useTranslation } from "react-i18next";
import * as aiApi from "../../../api/aiApi";

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

  if (!open) return null;

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
    <div className="doc-modal__overlay" onClick={onClose}>
      <div className="doc-modal" onClick={(e) => e.stopPropagation()}>
        <h2 className="doc-modal__title">{t("docDetail.editModal.title")}</h2>

        <form className="doc-modal__form" onSubmit={handleSubmit}>
          <label>
            {t("docDetail.editModal.name")}
            <input type="text" required value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          <button type="button" onClick={suggest} disabled={suggesting || submitting} className="doc-modal__submit">
            {suggesting ? t("docDetail.editModal.suggesting") : t("docDetail.editModal.aiSuggest")}
          </button>
          {suggestionError && <p style={{ color: "#e11", fontSize: 13 }}>{suggestionError}</p>}

          <label>
            {t("docDetail.editModal.description")}
            <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
          </label>

          <label style={{ fontWeight: 600 }}>{t("docDetail.editModal.subjectLabel")}</label>

          {subjectIds.length > 0 && (
            <div style={{ display: "flex", flexWrap: "wrap", gap: 6, margin: "6px 0" }}>
              {subjectIds.map((id) => {
                const s = subjects.find((x) => x.id === id);
                if (!s) return null;
                return (
                  <span
                    key={id}
                    onClick={() => toggleSubject(id)}
                    style={{
                      background: "#f3a712", color: "#fff", borderRadius: 999,
                      padding: "3px 12px", fontSize: 13, cursor: "pointer",
                      display: "inline-flex", alignItems: "center", gap: 6,
                    }}
                    title={t("docDetail.editModal.subjectRemoveHint")}
                  >
                    {s.name} <span style={{ fontWeight: 700 }}>×</span>
                  </span>
                );
              })}
            </div>
          )}

          <input
            type="text"
            placeholder={t("docDetail.editModal.subjectSearch")}
            value={subjectSearch}
            onChange={(e) => setSubjectSearch(e.target.value)}
            style={{ marginBottom: 8 }}
          />

          <div
            style={{
              maxHeight: 200,
              overflowY: "auto",
              border: "1px solid #e2e2e2",
              borderRadius: 10,
              background: "#fff",
            }}
          >
            {filteredSubjects.length === 0 && (
              <p style={{ color: "#888", fontSize: 13, margin: 0, padding: 12 }}>
                {t("docDetail.editModal.noSubject")}
              </p>
            )}
            {filteredSubjects.map((s) => {
              const checked = subjectIds.includes(s.id);
              return (
                <div
                  key={s.id}
                  onClick={() => toggleSubject(s.id)}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 10,
                    padding: "8px 12px",
                    cursor: "pointer",
                    borderBottom: "1px solid #f0f0f0",
                    background: checked ? "#fff7e6" : "transparent",
                    fontSize: 14,
                  }}
                >
                  <input
                    type="checkbox"
                    checked={checked}
                    readOnly
                    style={{ width: 16, height: 16, flexShrink: 0, pointerEvents: "none" }}
                  />
                  <span style={{ textAlign: "left" }}>{s.name}</span>
                </div>
              );
            })}
          </div>

          {subjectError && (
            <p style={{ color: "#e11", fontSize: 13, margin: "4px 0 0" }}>
              {t("docDetail.editModal.subjectRequired")}
            </p>
          )}

          <div className="doc-modal__actions">
            <button type="button" onClick={onClose} className="doc-modal__cancel">{t("common.actions.cancel")}</button>
            <button type="submit" disabled={submitting} className="doc-modal__submit">
              {submitting ? t("docDetail.editModal.saving") : t("docDetail.editModal.save")}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
