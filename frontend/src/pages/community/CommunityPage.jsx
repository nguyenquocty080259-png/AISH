import { Link } from "react-router-dom";
import { useCommunityPage } from "./useCommunityPage";
import { ROUTES, buildRoute } from "../../constants/routes";
import "../document/document.css";

export default function CommunityPage() {
  const { items, loading, keyword, setKeyword, sortBy, setSortBy, page, setPage, totalPages } =
    useCommunityPage();

  return (
    <div className="doc-page">
      <div className="doc-page__header">
        <h1 className="doc-page__title">Cộng đồng</h1>
      </div>

      <div className="doc-page__toolbar" style={{ display: "flex", gap: 8 }}>
        <input
          type="text"
          placeholder="Tìm tài liệu công khai..."
          value={keyword}
          onChange={(e) => { setPage(0); setKeyword(e.target.value); }}
          style={{ flex: 1 }}
        />
        <select value={sortBy} onChange={(e) => { setPage(0); setSortBy(e.target.value); }}>
          <option value="newest">Mới nhất</option>
          <option value="downloads">Tải nhiều</option>
          <option value="rating">Đánh giá cao</option>
        </select>
      </div>

      {loading ? (
        <p className="doc-empty">Đang tải...</p>
      ) : items.length === 0 ? (
        <p className="doc-empty">Chưa có tài liệu công khai nào.</p>
      ) : (
        <div className="doc-grid">
          {items.map((doc) => (
            <div key={doc.id} className="doc-card">
              {doc.subjectNames?.length > 0 && (
                <span className="doc-card__subject">{doc.subjectNames.join(", ")}</span>
              )}
              <Link
                to={buildRoute(ROUTES.DOCUMENT_DETAIL, { id: doc.id })}
                className="doc-card__title"
              >
                {doc.title}
              </Link>
              <p className="doc-card__desc">{doc.description}</p>
              <div className="doc-card__meta">
                <span>{doc.ownerName}</span>
                <span>★ {doc.averageRating?.toFixed?.(1) ?? "—"}</span>
                <span>⬇ {doc.downloadCount ?? 0}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div style={{ display: "flex", gap: 8, justifyContent: "center", marginTop: 16 }}>
          <button disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>← Trước</button>
          <span>Trang {page + 1} / {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>Sau →</button>
        </div>
      )}
    </div>
  );
}