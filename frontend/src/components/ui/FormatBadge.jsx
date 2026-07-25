import { useTranslation } from "react-i18next";
import Badge from "./Badge";
import "./FormatBadge.css";

const EXTENSION_LABELS = {
  pdf: "PDF",
  docx: "DOCX",
  doc: "DOC",
  xlsx: "XLSX",
  xls: "XLS",
  pptx: "PPTX",
  ppt: "PPT",
  txt: "TXT",
  png: "IMAGE",
  jpg: "IMAGE",
  jpeg: "IMAGE",
  gif: "IMAGE",
  webp: "IMAGE",
  bmp: "IMAGE",
  svg: "IMAGE",
};

function extensionOf(fileName) {
  if (!fileName) return "";
  const idx = fileName.lastIndexOf(".");
  if (idx <= 0 || idx === fileName.length - 1) return "";
  return fileName.slice(idx + 1).toLowerCase();
}

// Ưu tiên MIME type (đáng tin hơn) trước, phần mở rộng file làm lưới an toàn khi fileType
// thiếu/chung chung ("application/octet-stream"...). Trả về null khi không suy ra được gì
// (fileType rỗng và filename không có phần mở rộng) để component cha không render badge rỗng.
export function resolveFormatLabel(fileType, fileName) {
  const type = (fileType || "").toLowerCase();
  const ext = extensionOf(fileName);

  if (type.includes("image")) return "IMAGE";
  if (type.includes("pdf")) return "PDF";
  if (type.includes("wordprocessingml")) return "DOCX";
  if (type.includes("spreadsheetml")) return "XLSX";
  if (type.includes("presentationml")) return "PPTX";
  if (type === "text/plain") return "TXT";

  if (ext && EXTENSION_LABELS[ext]) return EXTENSION_LABELS[ext];
  if (ext) return ext.toUpperCase();
  return null;
}

export default function FormatBadge({ fileType, fileName, className = "" }) {
  const label = resolveFormatLabel(fileType, fileName);
  if (!label) return null;
  return (
    <Badge intent="info" className={`format-badge ${className}`.trim()}>
      {label}
    </Badge>
  );
}
