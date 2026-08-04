import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
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

function truncationNotice(sheet, t, locale) {
  if (!sheet.truncatedRows && !sheet.truncatedCols) return null;

  const fmt = (n) => n.toLocaleString(locale);
  if (sheet.truncatedRows && !sheet.truncatedCols) {
    return t("docDetail.viewers.xlsxTruncRows", { shown: fmt(MAX_ROWS), total: fmt(sheet.totalRows) });
  }
  if (sheet.truncatedCols && !sheet.truncatedRows) {
    return t("docDetail.viewers.xlsxTruncCols", { shown: fmt(MAX_COLS), total: fmt(sheet.totalCols) });
  }
  return t("docDetail.viewers.xlsxTruncBoth", {
    rowsShown: fmt(MAX_ROWS), rowsTotal: fmt(sheet.totalRows),
    colsShown: fmt(MAX_COLS), colsTotal: fmt(sheet.totalCols),
  });
}

// Xem trước file XLSX ngay trong trang: tải blob file gốc, parse bằng thư viện xlsx ở phía
// client, hiển thị từng sheet dạng bảng (giới hạn 500 dòng x 50 cột để tránh treo trình duyệt).
export default function XlsxViewer({ documentId }) {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
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
    return <div className="xlsx-viewer xlsx-viewer--error">{t("docDetail.viewers.xlsxError")}</div>;
  }

  if (sheets === null) {
    return <div className="xlsx-viewer xlsx-viewer--loading">{t("docDetail.viewers.xlsxLoading")}</div>;
  }

  if (sheets.length === 0) {
    return <div className="xlsx-viewer xlsx-viewer--empty">{t("docDetail.viewers.xlsxNoSheet")}</div>;
  }

  const sheet = sheets[activeSheet] ?? sheets[0];
  const notice = truncationNotice(sheet, t, locale);
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
        <div className="xlsx-viewer__empty">{t("docDetail.viewers.xlsxSheetEmpty")}</div>
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
