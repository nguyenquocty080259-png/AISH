import { useState } from "react";
import * as aiApi from "../../../api/aiApi";

// Modal sửa metadata: title, description, subjects. Prefill từ doc hiện tại.
export default function EditDocumentModal({ open, doc, subjects, submitting, onClose, onSubmit }) {
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
    catch (error) { setSuggestionError(error.response?.data?.message || error.message || "Không thể lấy gợi ý AI."); }
    finally { setSuggesting(false); }
  };

  return (
    <div className="doc-modal__overlay" onClick={onClose}>
      <div className="doc-modal" onClick={(e) => e.stopPropagation()}>
        <h2 className="doc-modal__title">Sửa tài liệu</h2>

        <form className="doc-modal__form" onSubmit={handleSubmit}>
          <label>
            Tên tài liệu
            <input type="text" required value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          <button type="button" onClick={suggest} disabled={suggesting || submitting} className="doc-modal__submit">
            {suggesting ? "Đang gợi ý..." : "AI gợi ý"}
          </button>
          {suggestionError && <p style={{ color: "#e11", fontSize: 13 }}>{suggestionError}</p>}

          <label>
            Mô tả
            <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
          </label>

          <label style={{ fontWeight: 600 }}>Môn học (chọn 1 hoặc nhiều)</label>

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
                    title="Bấm để bỏ chọn"
                  >
                    {s.name} <span style={{ fontWeight: 700 }}>×</span>
                  </span>
                );
              })}
            </div>
          )}

          <input
            type="text"
            placeholder="Tìm môn học..."
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
                Không tìm thấy môn.
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
              Vui lòng chọn ít nhất 1 môn học.
            </p>
          )}

          <div className="doc-modal__actions">
            <button type="button" onClick={onClose} className="doc-modal__cancel">Hủy</button>
            <button type="submit" disabled={submitting} className="doc-modal__submit">
              {submitting ? "Đang lưu..." : "Lưu"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
