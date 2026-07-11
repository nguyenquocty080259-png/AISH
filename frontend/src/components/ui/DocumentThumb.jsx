// Mini preview cho card.
// Ưu tiên thumbnail do BE sinh sẵn (thumbnailUrl) — nhanh, không tải cả file PDF/ảnh gốc.
// Nếu chưa có thumbnail: ảnh -> <img> file gốc, PDF -> iframe trang 1, còn lại -> icon.
import { useState } from "react";
import { uploadUrl, thumbnailUrl } from "../../lib/fileUrl";

export default function DocumentThumb({ doc }) {
  console.log("THUMB DEBUG:", { title: doc?.title, thumbnailUrl: doc?.thumbnailUrl, fileUrl: doc?.fileUrl, fileType: doc?.fileType });
  const [thumbFailed, setThumbFailed] = useState(false);

  const thumb = thumbnailUrl(doc);
  const fileUrl = uploadUrl(doc?.fileUrl);

  const type = (doc?.fileType || "").toLowerCase();
  const name = (doc?.fileName || "").toLowerCase();
  const isImage = type.includes("image") || /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(name);
  const isPdf = type.includes("pdf") || name.endsWith(".pdf");

  let icon = "📄";
  if (name.endsWith(".doc") || name.endsWith(".docx")) icon = "📝";
  else if (name.endsWith(".ppt") || name.endsWith(".pptx")) icon = "📊";
  else if (name.endsWith(".xls") || name.endsWith(".xlsx")) icon = "📈";
  else if (name.endsWith(".txt")) icon = "📃";

  // Có thumbnail sẵn và chưa lỗi -> dùng luôn (ưu tiên cao nhất).
  const useThumb = thumb && !thumbFailed;

  return (
    <div className="doc-thumb">
      <div className="doc-thumb__inner">
        {useThumb ? (
          <img
            className="doc-thumb__img"
            src={thumb}
            alt=""
            loading="lazy"
            onError={() => setThumbFailed(true)}
          />
        ) : fileUrl && isImage ? (
          <img className="doc-thumb__img" src={fileUrl} alt="" loading="lazy" />
        ) : fileUrl && isPdf ? (
          <iframe
            className="doc-thumb__pdf"
            src={`${fileUrl}#toolbar=0&navpanes=0&scrollbar=0&view=FitH`}
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