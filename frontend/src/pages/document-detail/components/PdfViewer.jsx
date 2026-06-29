import { useEffect, useRef, useState } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "react-pdf/dist/Page/AnnotationLayer.css";
import "react-pdf/dist/Page/TextLayer.css";

// Cấu hình worker cho pdfjs (bắt buộc), khớp đúng version đang dùng
pdfjs.GlobalWorkerOptions.workerSrc = `https://unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

export default function PdfViewer({ fileUrl }) {
  const containerRef = useRef(null);
  const [numPages, setNumPages] = useState(0);
  const [width, setWidth] = useState(800);
  const [error, setError] = useState(false);

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
        Không tải được PDF.{" "}
        <a href={fileUrl} target="_blank" rel="noreferrer">
          Mở trong tab mới
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
        loading={<div style={{ padding: 24, color: "#888" }}>Đang tải PDF...</div>}
      >
        {Array.from({ length: numPages }, (_, i) => (
          <div
            key={i}
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