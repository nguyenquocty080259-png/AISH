import { useState } from "react";

export default function UploadModal({ open, subjects, submitting, onClose, onSubmit }) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [subjectId, setSubjectId] = useState("");
  const [newSubjectName, setNewSubjectName] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [file, setFile] = useState(null);

  if (!open) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!file) return;
    const tags = tagsInput
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean);

    onSubmit({ title, description, subjectId, newSubjectName, tags, file });
  };

  return (
    <div className="doc-modal__overlay" onClick={onClose}>
      <div className="doc-modal" onClick={(e) => e.stopPropagation()}>
        <h2 className="doc-modal__title">Tải lên tài liệu</h2>

        <form className="doc-modal__form" onSubmit={handleSubmit}>
          <label>
            Tên tài liệu
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </label>

          <label>
            Mô tả
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </label>

          <label>
            Môn học
            <select
              value={subjectId}
              onChange={(e) => {
                setSubjectId(e.target.value);
                if (e.target.value) setNewSubjectName("");
              }}
            >
              <option value="">-- Chọn môn học --</option>
              {subjects.map((subject) => (
                <option key={subject.id} value={subject.id}>
                  {subject.name}
                </option>
              ))}
            </select>
          </label>

          {!subjectId && (
            <label>
              Hoặc nhập môn học mới
              <input
                type="text"
                value={newSubjectName}
                onChange={(e) => setNewSubjectName(e.target.value)}
                placeholder="VD: Lập trình Web"
              />
            </label>
          )}

          <label>
            Tags (phân tách bằng dấu phẩy)
            <input
              type="text"
              value={tagsInput}
              onChange={(e) => setTagsInput(e.target.value)}
              placeholder="java, swp391, slide"
            />
          </label>

          <label>
            File tài liệu
            <input
              type="file"
              required
              onChange={(e) => setFile(e.target.files[0])}
            />
          </label>

          <div className="doc-modal__actions">
            <button type="button" onClick={onClose} className="doc-modal__cancel">
              Hủy
            </button>
            <button type="submit" disabled={submitting} className="doc-modal__submit">
              {submitting ? "Đang tải lên..." : "Tải lên"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
