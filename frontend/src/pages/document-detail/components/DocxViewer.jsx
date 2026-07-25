import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { renderAsync } from "docx-preview";
import * as documentApi from "../../../api/documentApi";
import ExtractedTextViewer from "./ExtractedTextViewer";

// Không có trang thật cho DOCX (Tika báo page=null) nên không tự cuộn/tô sáng trong viewer
// này — citation callout (đoạn trích) vẫn hiển thị phía trên, do DocumentDetailPage render.
export default function DocxViewer({ documentId, highlightText }) {
  const { t } = useTranslation();
  const containerRef = useRef(null);
  const [status, setStatus] = useState("loading"); // loading | ready | error

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");

    documentApi
      .previewFile(documentId)
      .then((blob) => {
        if (cancelled || !containerRef.current) return;
        containerRef.current.innerHTML = "";
        return renderAsync(blob, containerRef.current);
      })
      .then(() => {
        if (!cancelled) setStatus("ready");
      })
      .catch(() => {
        if (!cancelled) setStatus("error");
      });

    return () => {
      cancelled = true;
    };
  }, [documentId]);

  // renderAsync lỗi (file hỏng, định dạng docx-preview chưa hỗ trợ...) -> rơi về text trích
  // xuất thay vì để trang trắng.
  if (status === "error") {
    return <ExtractedTextViewer documentId={documentId} highlightText={highlightText} />;
  }

  return (
    <div className="docx-viewer">
      {status === "loading" && <div className="docx-viewer__loading">{t("docDetail.viewers.docxLoading")}</div>}
      <div ref={containerRef} className="docx-viewer__content" />
    </div>
  );
}
