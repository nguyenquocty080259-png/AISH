import "./admin-pagination.css";

export default function AdminPagination({ page, totalPages, onPrev, onNext }) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <nav className="admin-pagination" aria-label="Phân trang quản trị">
      <button
        type="button"
        className="admin-pagination__button"
        disabled={page <= 0}
        onClick={onPrev}
      >
        ← Trước
      </button>
      <span className="admin-pagination__status">
        Trang {page + 1} / {totalPages}
      </span>
      <button
        type="button"
        className="admin-pagination__button"
        disabled={page >= totalPages - 1}
        onClick={onNext}
      >
        Sau →
      </button>
    </nav>
  );
}
