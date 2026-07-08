// Mini preview mờ cho card. Ảnh -> <img>, PDF -> iframe trang 1, loại khác -> icon.
// Không cần BE render thumbnail; dùng thẳng file đã có.
export default function DocumentThumb({ doc }) {
  const raw = doc?.fileUrl;
  const fileUrl = !raw
    ? null
    : raw.startsWith("http")
    ? raw
    : `http://localhost:8080/uploads/${raw}`;

  const type = (doc?.fileType || "").toLowerCase();
  const name = (doc?.fileName || "").toLowerCase();
  const isImage = type.includes("image") || /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(name);
  const isPdf = type.includes("pdf") || name.endsWith(".pdf");

  let icon = "📄";
  if (name.endsWith(".doc") || name.endsWith(".docx")) icon = "📝";
  else if (name.endsWith(".ppt") || name.endsWith(".pptx")) icon = "📊";
  else if (name.endsWith(".xls") || name.endsWith(".xlsx")) icon = "📈";
  else if (name.endsWith(".txt")) icon = "📃";

  return (
    <div className="doc-thumb">
      <div className="doc-thumb__inner">
        {fileUrl && isImage ? (
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