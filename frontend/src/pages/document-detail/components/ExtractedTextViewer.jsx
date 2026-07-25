import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import * as documentApi from "../../../api/documentApi";

// Rút gọn + chuẩn hoá snippet trước khi tìm trong text trích xuất — cùng logic với
// TextFileViewer (snippet lưu ở AI có thể bị cắt ngắn kèm "...", khoảng trắng/xuống dòng có
// thể khác nhẹ so với bản gốc).
function buildSearchNeedle(rawSnippet) {
  if (!rawSnippet) return "";
  let s = rawSnippet.trim();
  if (s.endsWith("...")) s = s.slice(0, -3).trim();
  return s.replace(/\s+/g, " ").slice(0, 80);
}

// Dùng cho PPTX (chưa có viewer trực quan riêng) và làm nhánh dự phòng khi DocxViewer render
// lỗi (T2). Nguồn dữ liệu là /preview-text (best-effort, POI/Tika phía backend) thay vì blob
// gốc như TextFileViewer.
export default function ExtractedTextViewer({ documentId, highlightText }) {
  const { t } = useTranslation();
  // undefined = đang tải, null = BE trả 204 (không trích được gì), string = có nội dung.
  const [content, setContent] = useState(undefined);
  const [error, setError] = useState(false);
  const markRef = useRef(null);
  const scrolledRef = useRef(false);

  useEffect(() => {
    let cancelled = false;
    setContent(undefined);
    setError(false);
    documentApi
      .previewText(documentId)
      .then((text) => {
        if (!cancelled) setContent(text);
      })
      .catch(() => {
        if (!cancelled) setError(true);
      });
    return () => {
      cancelled = true;
    };
  }, [documentId]);

  useEffect(() => {
    if (markRef.current && !scrolledRef.current) {
      markRef.current.scrollIntoView({ behavior: "smooth", block: "center" });
      scrolledRef.current = true;
    }
  }, [content]);

  if (error) {
    return <div className="txt-viewer txt-viewer--error">{t("docDetail.viewers.txtError")}</div>;
  }

  if (content === undefined) {
    return <div className="txt-viewer txt-viewer--loading">{t("docDetail.viewers.txtLoading")}</div>;
  }

  if (!content) {
    return (
      <div className="txt-viewer txt-viewer--empty">
        {t("docDetail.viewers.extractEmpty")}
      </div>
    );
  }

  const needle = buildSearchNeedle(highlightText);
  if (!needle) {
    return <pre className="txt-viewer">{content}</pre>;
  }

  const lowerContent = content.toLowerCase();
  const idx = lowerContent.indexOf(needle.toLowerCase());
  if (idx === -1) {
    return <pre className="txt-viewer">{content}</pre>;
  }

  const before = content.slice(0, idx);
  const match = content.slice(idx, idx + needle.length);
  const after = content.slice(idx + needle.length);

  return (
    <pre className="txt-viewer">
      {before}
      <mark ref={markRef} className="txt-viewer__highlight">
        {match}
      </mark>
      {after}
    </pre>
  );
}
