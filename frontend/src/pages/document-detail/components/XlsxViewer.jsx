import { useEffect, useState } from "react";
import * as XLSX from "@e965/xlsx";
import * as documentApi from "../../../api/documentApi";

const MAX_ROWS = 500;
const MAX_COLS = 50;

function parseWorkbook(buffer) {
  const workbook = XLSX.read(buffer, { type: "array" });
  return workbook.SheetNames.map((name) => {
    const sheet = workbook.Sheets[name];
    const range = sheet["!ref"] ? XLSX.utils.decode_range(sheet["!ref"]) : null;
    const totalRows = range ? range.e.r - range.s.r + 1 : 0;
    const totalCols = range ? range.e.c - range.s.c + 1 : 0;

    const allRows = XLSX.utils.sheet_to_json(sheet, { header: 1, blankrows: false });
    const rows = allRows.slice(0, MAX_ROWS).map((row) => row.slice(0, MAX_COLS));

    return {
      name,
      rows,
      totalRows,
      totalCols,
      truncatedRows: totalRows > MAX_ROWS,
      truncatedCols: totalCols > MAX_COLS,
    };
  });
}

function truncationNotice(sheet) {
  if (!sheet.truncatedRows && !sheet.truncatedCols) return null;

  const fmt = (n) => n.toLocaleString("vi-VN");
  if (sheet.truncatedRows && !sheet.truncatedCols) {
    return `Đang hiển thị ${fmt(MAX_ROWS)}/${fmt(sheet.totalRows)} dòng — tải file để xem đầy đủ.`;
  }
  if (sheet.truncatedCols && !sheet.truncatedRows) {
    return `Đang hiển thị ${fmt(MAX_COLS)}/${fmt(sheet.totalCols)} cột — tải file để xem đầy đủ.`;
  }
  return `Đang hiển thị ${fmt(MAX_ROWS)}/${fmt(sheet.totalRows)} dòng, ${fmt(MAX_COLS)}/${fmt(sheet.totalCols)} cột — tải file để xem đầy đủ.`;
}

export default function XlsxViewer({ documentId }) {
  const [sheets, setSheets] = useState(null);
  const [activeSheet, setActiveSheet] = useState(0);
  const [error, setError] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setSheets(null);
    setError(false);
    setActiveSheet(0);

    documentApi
      .previewFile(documentId)
      .then((blob) => blob.arrayBuffer())
      .then((buffer) => {
        if (cancelled) return;
        setSheets(parseWorkbook(buffer));
      })
      .catch(() => {
        if (!cancelled) setError(true);
      });

    return () => {
      cancelled = true;
    };
  }, [documentId]);

  if (error) {
    return <div className="xlsx-viewer xlsx-viewer--error">Không đọc được file bảng tính.</div>;
  }

  if (sheets === null) {
    return <div className="xlsx-viewer xlsx-viewer--loading">Đang tải bảng tính...</div>;
  }

  if (sheets.length === 0) {
    return <div className="xlsx-viewer xlsx-viewer--empty">File không có sheet nào.</div>;
  }

  const sheet = sheets[activeSheet] ?? sheets[0];
  const notice = truncationNotice(sheet);
  const colCount = Math.min(sheet.totalCols, MAX_COLS);

  return (
    <div className="xlsx-viewer">
      <div className="xlsx-viewer__tabs">
        {sheets.map((s, i) => (
          <button
            key={s.name}
            type="button"
            className={`xlsx-viewer__tab ${i === activeSheet ? "xlsx-viewer__tab--active" : ""}`}
            onClick={() => setActiveSheet(i)}
          >
            {s.name}
          </button>
        ))}
      </div>

      {notice && <div className="xlsx-viewer__notice">{notice}</div>}

      {sheet.rows.length === 0 ? (
        <div className="xlsx-viewer__empty">Sheet này không có dữ liệu.</div>
      ) : (
        <div className="xlsx-viewer__table-wrap">
          <table className="xlsx-viewer__table">
            <tbody>
              {sheet.rows.map((row, ri) => (
                <tr key={ri}>
                  {Array.from({ length: colCount }, (_, ci) => (
                    <td key={ci}>{row[ci] ?? ""}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
