import { useEffect, useRef, useState } from "react";
import * as documentApi from "../../../api/documentApi";

// Rút gọn + chuẩn hoá snippet trước khi tìm trong nội dung file — snippet lưu ở AI có thể
// bị backend cắt ngắn kèm "..." (không thật sự có trong file gốc), và khoảng trắng/xuống
// dòng có thể khác nhẹ so với bản gốc, nên chỉ dùng một đoạn đầu ngắn để tăng khả năng khớp.
function buildSearchNeedle(rawSnippet) {
  if (!rawSnippet) return "";
  let s = rawSnippet.trim();
  if (s.endsWith("...")) s = s.slice(0, -3).trim();
  return s.replace(/\s+/g, " ").slice(0, 80);
}

export default function TextFileViewer({ documentId, highlightText }) {
  const [content, setContent] = useState(null);
  const [error, setError] = useState(false);
  const markRef = useRef(null);
  const scrolledRef = useRef(false);

  useEffect(() => {
    let cancelled = false;
    documentApi
      .previewFile(documentId)
      .then((blob) => blob.text())
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
    return (
      <div className="txt-viewer txt-viewer--error">Không tải được nội dung file.</div>
    );
  }

  if (content === null) {
    return <div className="txt-viewer txt-viewer--loading">Đang tải nội dung...</div>;
  }

  const needle = buildSearchNeedle(highlightText);
  if (!needle) {
    return (
      <pre className="txt-viewer">{content}</pre>
    );
  }

  // Tìm không phân biệt hoa/thường, nhưng vẫn hiển thị đúng nguyên văn gốc.
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
