// Giữ NGUYÊN props: page, totalPages, onPrev, onNext.
export default function AdminPagination({ page, totalPages, onPrev, onNext }) {
  if (totalPages <= 1) return null;
  return (
    <nav className="mt-6 flex items-center justify-center gap-4 text-sm" aria-label="Phân trang quản trị">
      <button type="button" disabled={page <= 0} onClick={onPrev}
        className="rounded-input border border-border px-4 py-2 text-secondary transition-colors hover:border-primary hover:text-primary disabled:opacity-50">
        ← Trước
      </button>
      <span className="text-secondary">Trang {page + 1} / {totalPages}</span>
      <button type="button" disabled={page >= totalPages - 1} onClick={onNext}
        className="rounded-input border border-border px-4 py-2 text-secondary transition-colors hover:border-primary hover:text-primary disabled:opacity-50">
        Sau →
      </button>
    </nav>
  );
}
