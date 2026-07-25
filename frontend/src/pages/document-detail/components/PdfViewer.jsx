import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Document, Page, pdfjs } from "react-pdf";
import "react-pdf/dist/Page/AnnotationLayer.css";
import "react-pdf/dist/Page/TextLayer.css";

// Cấu hình worker cho pdfjs (bắt buộc), khớp đúng version đang dùng
pdfjs.GlobalWorkerOptions.workerSrc = `https://unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

// Rút gọn + chuẩn hoá snippet trước khi tìm trong text layer — snippet lưu ở AI có thể bị
// backend cắt ngắn kèm "..." (không thật sự có trong PDF gốc), và text layer của pdf.js có
// thể tách câu thành nhiều span với khoảng trắng khác bản gốc, nên chỉ dùng một đoạn đầu
// ngắn (best-effort search), không phải khớp ký tự chính xác tuyệt đối.
function buildSearchNeedle(rawSnippet) {
  if (!rawSnippet) return "";
  let s = rawSnippet.trim();
  if (s.endsWith("...")) s = s.slice(0, -3).trim();
  return s.replace(/\s+/g, " ").toLowerCase().slice(0, 80);
}

// Best-effort: nối text của các span trong text layer (cách nhau 1 khoảng trắng), tìm đoạn
// khớp needle, rồi tô sáng NGUYÊN VẸN các span nằm trong vùng khớp. Không cắt span ở biên
// khớp (không tách 1 span làm đôi) — đơn giản, đủ để định vị mắt thường, không cần chính xác
// từng ký tự vì vốn dĩ không có char offset lưu trong DB.
function highlightMatchInTextLayer(container, needle) {
  if (!container || !needle) return false;
  const spans = Array.from(container.querySelectorAll("span"));
  if (spans.length === 0) return false;

  const texts = spans.map((el) => (el.textContent || "").toLowerCase());
  const offsets = [];
  let pos = 0;
  texts.forEach((t) => {
    offsets.push(pos);
    pos += t.length + 1; // +1 cho khoảng trắng nối giữa các span khi ghép chuỗi
  });
  const joined = texts.join(" ");

  const idx = joined.indexOf(needle);
  if (idx === -1) return false;
  const matchEnd = idx + needle.length;

  spans.forEach((el, i) => {
    const start = offsets[i];
    const end = start + texts[i].length;
    if (end > idx && start < matchEnd) {
      el.classList.add("pdf-highlight-mark");
    }
  });
  return true;
}

export default function PdfViewer({ fileUrl, initialPage, highlightText }) {
  const { t } = useTranslation();
  const containerRef = useRef(null);
  const pageRefs = useRef({});
  const scrolledRef = useRef(false);
  const [numPages, setNumPages] = useState(0);
  const [width, setWidth] = useState(800);
  const [error, setError] = useState(false);

  // Khi trang/nội dung PDF đổi (vd. re-ingest), cho phép scroll-tới-trang chạy lại.
  useEffect(() => {
    scrolledRef.current = false;
  }, [fileUrl, initialPage]);

  const handlePageTextLayerReady = (pageNum) => {
    if (pageNum !== initialPage) return;
    const pageEl = pageRefs.current[pageNum];
    if (!pageEl) return;

    if (!scrolledRef.current) {
      pageEl.scrollIntoView({ behavior: "smooth", block: "start" });
      scrolledRef.current = true;
    }
    if (highlightText) {
      const textLayer = pageEl.querySelector(".react-pdf__Page__textLayer");
      highlightMatchInTextLayer(textLayer, buildSearchNeedle(highlightText));
    }
  };

  // Tự co giãn theo bề rộng khung chứa
  useEffect(() => {
    const update = () => {
      if (containerRef.current) {
        setWidth(Math.min(containerRef.current.clientWidth - 32, 900));
      }
    };
    update();
    window.addEventListener("resize", update);
    return () => window.removeEventListener("resize", update);
  }, []);

  if (error) {
    return (
      <div style={{ padding: 24, textAlign: "center", color: "#888" }}>
        {t("docDetail.viewers.pdfError")}{" "}
        <a href={fileUrl} target="_blank" rel="noreferrer">
          {t("docDetail.viewers.openNewTab")}
        </a>
      </div>
    );
  }

  return (
    <div
      ref={containerRef}
      style={{
        background: "#f3f4f6",
        borderRadius: 12,
        padding: 16,
        maxHeight: "80vh",
        overflowY: "auto",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: 16,
      }}
    >
      <Document
        file={fileUrl}
        onLoadSuccess={({ numPages }) => setNumPages(numPages)}
        onLoadError={() => setError(true)}
        loading={<div style={{ padding: 24, color: "#888" }}>{t("docDetail.viewers.loadingPdf")}</div>}
      >
        {Array.from({ length: numPages }, (_, i) => (
          <div
            key={i}
            ref={(el) => (pageRefs.current[i + 1] = el)}
            style={{
              marginBottom: 16,
              boxShadow: "0 2px 12px rgba(0,0,0,0.12)",
              borderRadius: 4,
              overflow: "hidden",
              position: "relative",
            }}
          >
            <Page
              pageNumber={i + 1}
              width={width}
              renderTextLayer={true}
              renderAnnotationLayer={false}
              onRenderTextLayerSuccess={() => handlePageTextLayerReady(i + 1)}
            />
            <span
              style={{
                position: "absolute",
                bottom: 8,
                right: 8,
                background: "rgba(0,0,0,0.6)",
                color: "#fff",
                fontSize: 12,
                padding: "2px 8px",
                borderRadius: 12,
              }}
            >
              {i + 1} / {numPages}
            </span>
          </div>
        ))}
      </Document>
    </div>
  );
}