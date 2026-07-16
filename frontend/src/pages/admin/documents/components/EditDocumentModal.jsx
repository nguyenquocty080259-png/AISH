import { useEffect, useState } from "react";
import Button from "../../../../components/ui/Button";
import Modal from "../../../../components/ui/Modal";
import * as aiApi from "../../../../api/aiApi";

// Modal Admin sửa metadata tài liệu (title/description/subjectIds) — field shape đồng bộ với
// document-detail/hooks/useDocumentDetailPage.js (handleUpdateDocument).
export default function EditDocumentModal({ open, doc, subjects, submitting, loading, onClose, onSubmit }) {
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
    catch (error) { setSuggestionError(error.response?.data?.message || error.message || "Không thể lấy gợi ý AI."); }
    finally { setSuggesting(false); }
  };

  return (
    <Modal open={open} onClose={onClose} title="Sửa tài liệu">
      {loading ? (
        <p className="admin-documents-page__loading">Đang tải dữ liệu tài liệu...</p>
      ) : (
      <form className="admin-subjects-form" onSubmit={handleSubmit}>
        <label className="admin-subjects-form__field">
          Tên tài liệu
          <input type="text" required value={title} onChange={(e) => setTitle(e.target.value)} />
        </label>
        <Button type="button" variant="secondary" onClick={suggest} disabled={suggesting || submitting}>
          {suggesting ? "Đang gợi ý..." : "AI gợi ý"}
        </Button>
        {suggestionError && <p className="admin-documents-page__subject-error">{suggestionError}</p>}

        <label className="admin-subjects-form__field">
          Mô tả
          <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>

        <div className="admin-subjects-form__field">
          Môn học (chọn ít nhất 1)
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
            <p className="admin-documents-page__subject-error">Vui lòng chọn ít nhất 1 môn học.</p>
          )}
        </div>

        <div className="admin-subjects-form__actions">
          <Button type="button" variant="secondary" onClick={onClose} disabled={submitting}>
            Hủy
          </Button>
          <Button type="submit" variant="primary" disabled={submitting}>
            {submitting ? "Đang lưu..." : "Lưu"}
          </Button>
        </div>
      </form>
      )}
    </Modal>
  );
}
