import { useState } from "react";
import { useTranslation } from "react-i18next";
import { formatBytes } from "../../../components/ui/StorageUsageBar";
import Modal from "../../../components/ui/Modal";
import Button from "../../../components/ui/Button";
import { Field, Input, Textarea } from "../../../components/ui/Field";

// Trả về thông báo lỗi (đã dịch) nếu file vượt giới hạn 1 tệp HOẶC vượt quota còn lại cho
// storage đã chọn, null nếu ổn. storageUsage null (chưa tải được / lỗi) -> fail-open, không
// chặn gì ở FE (BE vẫn là chốt chặn thật - xem DocumentServiceImpl.enforceUploadSizeLimit/
// enforceUploadQuota).
function checkSizeLimit(file, storage, storageUsage, t) {
  if (!file || !storageUsage) return null;
  const {
    usedLocalBytes, usedCloudBytes,
    quotaLocalBytes, quotaCloudBytes,
    maxFileLocalBytes, maxFileCloudBytes,
  } = storageUsage;

  if (storage === "LOCAL" || storage === "BOTH") {
    if (maxFileLocalBytes != null && file.size > maxFileLocalBytes) {
      return t("documents.upload_modal.sizeLocal", { file: formatBytes(file.size), limit: formatBytes(maxFileLocalBytes) });
    }
    const remainingLocal = quotaLocalBytes - usedLocalBytes;
    if (quotaLocalBytes != null && file.size > remainingLocal) {
      return t("documents.upload_modal.quotaLocal", { remaining: formatBytes(Math.max(remainingLocal, 0)), file: formatBytes(file.size) });
    }
  }
  if (storage === "CLOUD" || storage === "BOTH") {
    if (maxFileCloudBytes != null && file.size > maxFileCloudBytes) {
      return t("documents.upload_modal.sizeCloud", { file: formatBytes(file.size), limit: formatBytes(maxFileCloudBytes) });
    }
    const remainingCloud = quotaCloudBytes - usedCloudBytes;
    if (quotaCloudBytes != null && file.size > remainingCloud) {
      return t("documents.upload_modal.quotaCloud", { remaining: formatBytes(Math.max(remainingCloud, 0)), file: formatBytes(file.size) });
    }
  }
  return null;
}

// Text "Còn lại: ..." cho nơi lưu đang chọn, hiển thị trước khi user chọn file.
function remainingSpaceText(storage, storageUsage, t) {
  if (!storageUsage) return null;
  const { usedLocalBytes, usedCloudBytes, quotaLocalBytes, quotaCloudBytes } = storageUsage;
  const parts = [];
  if (storage === "LOCAL" || storage === "BOTH") {
    parts.push(t("documents.upload_modal.remainingLocal", { size: formatBytes(Math.max(quotaLocalBytes - usedLocalBytes, 0)) }));
  }
  if (storage === "CLOUD" || storage === "BOTH") {
    parts.push(t("documents.upload_modal.remainingCloud", { size: formatBytes(Math.max(quotaCloudBytes - usedCloudBytes, 0)) }));
  }
  return t("documents.upload_modal.remainingPrefix") + parts.join(", ");
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

// Thông báo lỗi (đã dịch) nếu đuôi tệp không thuộc allowlist, null nếu ổn / chưa biết allowlist
// (fail-open, BE vẫn là chốt chặn cuối - xem UploadFileTypeService).
function checkFileType(file, allowedFileTypes, t) {
  if (!file || !allowedFileTypes || allowedFileTypes.length === 0) return null;
  const ext = extensionOf(file.name);
  if (!ext || !allowedFileTypes.includes(ext)) {
    const types = allowedFileTypes.join(", ");
    return ext
      ? t("documents.upload_modal.fileTypeWithExt", { ext, types })
      : t("documents.upload_modal.fileTypeNoExt", { types });
  }
  return null;
}

export default function UploadModal({ open, subjects, submitting, storageUsage, allowedFileTypes, onClose, onSubmit }) {
  const { t } = useTranslation();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [subjectIds, setSubjectIds] = useState([]);
  const [subjectSearch, setSubjectSearch] = useState("");
  const [subjectError, setSubjectError] = useState(false);
  const [file, setFile] = useState(null);
  // Nơi lưu file: LOCAL (máy chủ) | CLOUD (Cloudinary) | BOTH (lưu cả 2)
  const [storage, setStorage] = useState("LOCAL");

  const STORAGE_OPTIONS = [
    { value: "LOCAL", label: t("documents.upload_modal.storageLocal"), hint: t("documents.upload_modal.storageLocalHint") },
    { value: "CLOUD", label: t("documents.upload_modal.storageCloud"), hint: t("documents.upload_modal.storageCloudHint") },
    { value: "BOTH", label: t("documents.upload_modal.storageBoth"), hint: t("documents.upload_modal.storageBothHint") },
  ];

  const sizeError = checkSizeLimit(file, storage, storageUsage, t);
  const typeError = checkFileType(file, allowedFileTypes, t);
  const remainingText = remainingSpaceText(storage, storageUsage, t);
  // Thuộc tính accept cho input file - chỉ set khi biết allowlist, để hộp thoại chọn tệp lọc sẵn.
  const acceptAttr =
    allowedFileTypes && allowedFileTypes.length > 0
      ? allowedFileTypes.map((ext) => "." + ext).join(",")
      : undefined;

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
    <Modal
      open={open}
      onClose={onClose}
      title={t("documents.upload_modal.title")}
      size="lg"
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={submitting}>
            {t("common.actions.cancel")}
          </Button>
          <Button
            type="submit"
            form="upload-modal-form"
            loading={submitting}
            disabled={!!sizeError || !!typeError}
          >
            {submitting ? t("documents.upload_modal.submitting") : t("documents.upload_modal.submit")}
          </Button>
        </>
      }
    >
      <form id="upload-modal-form" className="doc-form__form" onSubmit={handleSubmit}>
        <Input
          label={t("documents.upload_modal.name")}
          required
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />

        <div>
          <Button variant="secondary" size="sm" disabled title={t("documents.upload_modal.aiSuggestHint")}>
            {t("documents.upload_modal.aiSuggest")}
          </Button>
          <p className="doc-form__hint">{t("documents.upload_modal.aiSuggestHint")}</p>
        </div>

        <Textarea
          label={t("documents.upload_modal.description")}
          rows={3}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        {/* ===== Chọn môn học ===== */}
        <Field
          id="upload-modal-subjects"
          label={t("documents.upload_modal.subjectLabel")}
          error={subjectError ? t("documents.upload_modal.subjectRequired") : undefined}
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
                    title={t("documents.upload_modal.subjectRemoveHint")}
                  >
                    {s.name}
                    <span className="doc-form__chip-x" aria-hidden="true">×</span>
                  </button>
                );
              })}
            </div>
          )}

          <Input
            id="upload-modal-subjects"
            type="search"
            placeholder={t("documents.upload_modal.subjectSearch")}
            aria-label={t("documents.upload_modal.subjectSearch")}
            value={subjectSearch}
            onChange={(e) => setSubjectSearch(e.target.value)}
          />

          <div className="doc-form__subject-list">
            {filteredSubjects.length === 0 && (
              <p className="doc-form__subject-empty">{t("documents.upload_modal.noSubject")}</p>
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

        {/* ===== Chọn tệp ===== */}
        <Field
          id="upload-modal-file"
          label={t("documents.upload_modal.fileLabel")}
          required
          hint={
            allowedFileTypes && allowedFileTypes.length > 0
              ? t("documents.upload_modal.allowedTypes", { types: allowedFileTypes.join(", ") })
              : undefined
          }
          error={typeError || undefined}
        >
          <input
            id="upload-modal-file"
            className="doc-form__file has-custom-focus"
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
        </Field>

        {/* ===== Nơi lưu ===== */}
        <Field
          id="upload-modal-storage"
          label={t("documents.upload_modal.storageLabel")}
          hint={
            [STORAGE_OPTIONS.find((o) => o.value === storage)?.hint, remainingText]
              .filter(Boolean)
              .join(" · ")
          }
          error={sizeError || undefined}
        >
          <div className="doc-form__storage" role="radiogroup" aria-label={t("documents.upload_modal.storageLabel")}>
            {STORAGE_OPTIONS.map((opt) => {
              const active = storage === opt.value;
              return (
                <button
                  key={opt.value}
                  type="button"
                  role="radio"
                  aria-checked={active}
                  onClick={() => setStorage(opt.value)}
                  title={opt.hint}
                  className={`doc-form__storage-opt has-custom-focus ${
                    active ? "doc-form__storage-opt--active" : ""
                  }`.trim()}
                >
                  {opt.label}
                </button>
              );
            })}
          </div>
        </Field>
      </form>
    </Modal>
  );
}
