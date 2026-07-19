// Mini preview cho card.
// Ưu tiên thumbnail do BE sinh sẵn (endpoint /thumbnail có xác thực) — nhanh, không tải cả file
// PDF/ảnh gốc. Nếu chưa có thumbnail (204): ảnh/PDF -> lấy blob /preview (có xác thực) và render
// như cũ, còn lại -> icon. Không phải chủ sở hữu và tài liệu không PUBLIC (403) -> hiển thị dòng
// chữ thay vì ảnh vỡ.
import { useEffect, useState } from "react";
import { thumbnailUrl } from "../../lib/fileUrl";
import * as documentApi from "../../api/documentApi";
import "./doc-thumb.css";

export default function DocumentThumb({ doc }) {
  const [blobUrl, setBlobUrl] = useState(null);
  const [kind, setKind] = useState(null); // "thumb" | "image" | "pdf" | null (-> icon)
  const [imgError, setImgError] = useState(false);
  const [forbidden, setForbidden] = useState(false);

  const type = (doc?.fileType || "").toLowerCase();
  const name = (doc?.fileName || "").toLowerCase();
  const isImage = type.includes("image") || /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(name);
  const isPdf = type.includes("pdf") || name.endsWith(".pdf");

  let icon = "📄";
  if (name.endsWith(".doc") || name.endsWith(".docx")) icon = "📝";
  else if (name.endsWith(".ppt") || name.endsWith(".pptx")) icon = "📊";
  else if (name.endsWith(".xls") || name.endsWith(".xlsx")) icon = "📈";
  else if (name.endsWith(".txt")) icon = "📃";

  useEffect(() => {
    setBlobUrl(null);
    setKind(null);
    setImgError(false);
    setForbidden(false);

    // thumbnailUrl tuyệt đối (Cloudinary) -> dùng thẳng, không cần fetch có xác thực.
    const cloudThumb = thumbnailUrl(doc);
    if (cloudThumb) {
      setBlobUrl(cloudThumb);
      setKind("thumb");
      return;
    }

    if (!doc?.id) return;

    let cancelled = false;
    let objectUrl = null;

    documentApi
      .getThumbnail(doc.id)
      .then((blob) => {
        if (cancelled) return null;
        if (blob) {
          objectUrl = window.URL.createObjectURL(blob);
          setBlobUrl(objectUrl);
          setKind("thumb");
          return null;
        }
        // Không có thumbnail (204) -> fallback ảnh/PDF gốc qua /preview, còn lại giữ icon.
        return isImage || isPdf ? documentApi.previewFile(doc.id) : null;
      })
      .then((previewBlob) => {
        if (cancelled || !previewBlob) return;
        objectUrl = window.URL.createObjectURL(previewBlob);
        setBlobUrl(objectUrl);
        setKind(isPdf ? "pdf" : "image");
      })
      .catch((err) => {
        if (!cancelled && err?.status === 403) setForbidden(true);
      });

    return () => {
      cancelled = true;
      if (objectUrl) window.URL.revokeObjectURL(objectUrl);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [doc?.id, doc?.thumbnailUrl, doc?.fileType, doc?.fileName]);

  return (
    <div className="doc-thumb">
      <div className="doc-thumb__inner">
        {forbidden ? (
          <div
            className="doc-thumb__icon"
            style={{ fontSize: 13, padding: 8, textAlign: "center", lineHeight: 1.3 }}
          >
            Bạn không có quyền xem tài liệu này
          </div>
        ) : (kind === "thumb" || kind === "image") && !imgError ? (
          <img
            className="doc-thumb__img"
            src={blobUrl}
            alt=""
            loading="lazy"
            onError={() => setImgError(true)}
          />
        ) : kind === "pdf" ? (
          <iframe
            className="doc-thumb__pdf"
            src={`${blobUrl}#toolbar=0&navpanes=0&scrollbar=0&view=FitH`}
            title="preview"
            tabIndex={-1}
          />
        ) : (
          <div className="doc-thumb__icon">{icon}</div>
        )}
      </div>
    </div>
  );
}
