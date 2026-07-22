import { useState } from "react";
import { formatBytes } from "../../../components/ui/StorageUsageBar";

// Trả về thông báo lỗi tiếng Việt nếu file vượt giới hạn 1 tệp HOẶC vượt quota còn lại cho
// storage đã chọn, null nếu ổn. storageUsage null (chưa tải được / lỗi) -> fail-open, không
// chặn gì ở FE (BE vẫn là chốt chặn thật - xem DocumentServiceImpl.enforceUploadSizeLimit/
// enforceUploadQuota).
function checkSizeLimit(file, storage, storageUsage) {
  if (!file || !storageUsage) return null;
  const {
    usedLocalBytes, usedCloudBytes,
    quotaLocalBytes, quotaCloudBytes,
    maxFileLocalBytes, maxFileCloudBytes,
  } = storageUsage;

  if (storage === "LOCAL" || storage === "BOTH") {
    if (maxFileLocalBytes != null && file.size > maxFileLocalBytes) {
      return `Tệp ${formatBytes(file.size)} vượt giới hạn ${formatBytes(maxFileLocalBytes)} cho nơi lưu Máy chủ.`;
    }
    const remainingLocal = quotaLocalBytes - usedLocalBytes;
    if (quotaLocalBytes != null && file.size > remainingLocal) {
      return `Dung lượng còn lại ${formatBytes(Math.max(remainingLocal, 0))} không đủ cho tệp ${formatBytes(file.size)} ở nơi lưu Máy chủ.`;
    }
  }
  if (storage === "CLOUD" || storage === "BOTH") {
    if (maxFileCloudBytes != null && file.size > maxFileCloudBytes) {
      return `Tệp ${formatBytes(file.size)} vượt giới hạn ${formatBytes(maxFileCloudBytes)} cho nơi lưu Cloud.`;
    }
    const remainingCloud = quotaCloudBytes - usedCloudBytes;
    if (quotaCloudBytes != null && file.size > remainingCloud) {
      return `Dung lượng còn lại ${formatBytes(Math.max(remainingCloud, 0))} không đủ cho tệp ${formatBytes(file.size)} ở nơi lưu Cloud.`;
    }
  }
  return null;
}

// Text "Còn lại: ..." cho nơi lưu đang chọn, hiển thị trước khi user chọn file.
function remainingSpaceText(storage, storageUsage) {
  if (!storageUsage) return null;
  const { usedLocalBytes, usedCloudBytes, quotaLocalBytes, quotaCloudBytes } = storageUsage;
  const parts = [];
  if (storage === "LOCAL" || storage === "BOTH") {
    parts.push(`Máy chủ: ${formatBytes(Math.max(quotaLocalBytes - usedLocalBytes, 0))}`);
  }
  if (storage === "CLOUD" || storage === "BOTH") {
    parts.push(`Cloud: ${formatBytes(Math.max(quotaCloudBytes - usedCloudBytes, 0))}`);
  }
  return "Còn lại - " + parts.join(", ");
}

// Đuôi tệp của 1 tên file, viết thường, không kèm dấu chấm ("bao-cao.PDF" -> "pdf"). "" nếu
// không có đuôi.
function extensionOf(filename) {
  if (!filename) return "";
  const name = filename.trim().toLowerCase();
  const dot = name.lastIndexOf(".");
  if (dot < 0 || dot === name.length - 1) return "";
  return name.slice(dot + 1);
}

// Thông báo lỗi nếu đuôi tệp không thuộc allowlist, null nếu ổn / chưa biết allowlist (fail-open,
// BE vẫn là chốt chặn cuối - xem UploadFileTypeService).
function checkFileType(file, allowedFileTypes) {
  if (!file || !allowedFileTypes || allowedFileTypes.length === 0) return null;
  const ext = extensionOf(file.name);
  if (!ext || !allowedFileTypes.includes(ext)) {
    return `Loại tệp${ext ? " ." + ext : ""} không được phép. Các loại được phép: ${allowedFileTypes.join(", ")}.`;
  }
  return null;
}

export default function UploadModal({ open, subjects, submitting, storageUsage, allowedFileTypes, onClose, onSubmit }) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [subjectIds, setSubjectIds] = useState([]);
  const [subjectSearch, setSubjectSearch] = useState("");
  const [subjectError, setSubjectError] = useState(false);
  const [file, setFile] = useState(null);
  // Nơi lưu file: LOCAL (máy chủ) | CLOUD (Cloudinary) | BOTH (lưu cả 2)
  const [storage, setStorage] = useState("LOCAL");

  const STORAGE_OPTIONS = [
    { value: "LOCAL", label: "Máy chủ", hint: "Lưu trên server (nhanh, mặc định)" },
    { value: "CLOUD", label: "Cloud", hint: "Lưu trên Cloudinary" },
    { value: "BOTH", label: "Cả hai", hint: "Lưu cả server lẫn Cloudinary" },
  ];

  const sizeError = checkSizeLimit(file, storage, storageUsage);
  const typeError = checkFileType(file, allowedFileTypes);
  const remainingText = remainingSpaceText(storage, storageUsage);
  // Thuộc tính accept cho input file - chỉ set khi biết allowlist, để hộp thoại chọn tệp lọc sẵn.
  const acceptAttr =
    allowedFileTypes && allowedFileTypes.length > 0
      ? allowedFileTypes.map((ext) => "." + ext).join(",")
      : undefined;

  if (!open) return null;

  // lọc môn theo ô tìm kiếm
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
    if (!file) return;
    if (subjectIds.length === 0) {
      setSubjectError(true);
      return;
    }
    if (sizeError || typeError) return;
   onSubmit({ title, description, subjectIds, file, storage });
  };

  return (
    <div className="doc-modal__overlay" onClick={onClose}>
      <div className="doc-modal" onClick={(e) => e.stopPropagation()}>
        <h2 className="doc-modal__title">Tải lên tài liệu</h2>

        <form className="doc-modal__form" onSubmit={handleSubmit}>
          <label>
            Tên tài liệu
            <input type="text" required value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          <button type="button" disabled title="Tải lên xong, mở Sửa tài liệu để AI gợi ý"
            style={{ opacity: 0.65, cursor: "not-allowed" }} className="doc-modal__submit">
            AI gợi ý
          </button>
          <p style={{ color: "#777", fontSize: 12 }}>Tải lên xong, mở Sửa tài liệu để AI gợi ý</p>

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
                  onMouseEnter={(e) => {
                    if (!checked) e.currentTarget.style.background = "#f7f7f7";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.background = checked ? "#fff7e6" : "transparent";
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

          <label>
            File tài liệu
            <input
              type="file"
              required
              accept={acceptAttr}
              onChange={(e) => {
                const f = e.target.files[0];
                if (!f) return;
                setFile(f);
                if (!title.trim()) setTitle(f.name.replace(/\.[^/.]+$/, ""));
              }}
            />
          </label>
          {allowedFileTypes && allowedFileTypes.length > 0 && (
            <p style={{ color: "#888", fontSize: 12, margin: "2px 0 0" }}>
              Loại tệp được phép: {allowedFileTypes.join(", ")}.
            </p>
          )}
          {typeError && (
            <p style={{ color: "#e11", fontSize: 13, margin: "4px 0 0" }}>{typeError}</p>
          )}
          <label style={{ fontWeight: 600 }}>Nơi lưu trữ</label>
          <div style={{ display: "flex", gap: 8 }}>
            {STORAGE_OPTIONS.map((opt) => {
              const active = storage === opt.value;
              return (
                <button
                  key={opt.value}
                  type="button"
                  onClick={() => setStorage(opt.value)}
                  title={opt.hint}
                  style={{
                    flex: 1,
                    padding: "8px 10px",
                    borderRadius: 10,
                    cursor: "pointer",
                    fontSize: 14,
                    fontWeight: active ? 700 : 400,
                    border: active ? "2px solid #f3a712" : "1px solid #e2e2e2",
                    background: active ? "#fff7e6" : "#fff",
                    color: active ? "#b97b00" : "#444",
                  }}
                >
                  {opt.label}
                </button>
              );
            })}
          </div>
          <p style={{ color: "#888", fontSize: 12, margin: "2px 0 0" }}>
            {STORAGE_OPTIONS.find((o) => o.value === storage)?.hint}
          </p>
          {remainingText && (
            <p style={{ color: "#888", fontSize: 12, margin: "2px 0 0" }}>{remainingText}</p>
          )}

          {sizeError && (
            <p style={{ color: "#e11", fontSize: 13, margin: "4px 0 0" }}>{sizeError}</p>
          )}

          <div className="doc-modal__actions">
            <button type="button" onClick={onClose} className="doc-modal__cancel">Hủy</button>
            <button type="submit" disabled={submitting || !!sizeError || !!typeError} className="doc-modal__submit">
              {submitting ? "Đang tải lên..." : "Tải lên"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
