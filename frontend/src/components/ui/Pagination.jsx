import "./Pagination.css";

// Sinh dãy trang có dấu "…" khi nhiều trang: 1 … 4 5 [6] 7 8 … 20.
function pageItems(current, total) {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);

  const items = [1];
  const start = Math.max(2, current - 1);
  const end = Math.min(total - 1, current + 1);

  if (start > 2) items.push("start-ellipsis");
  for (let p = start; p <= end; p += 1) items.push(p);
  if (end < total - 1) items.push("end-ellipsis");
  items.push(total);

  return items;
}

// page: số trang HIỆN TẠI tính từ 1. totalPages: tổng số trang.
// onPageChange(nextPage) — cũng tính từ 1, nơi gọi tự quy đổi nếu API đánh số từ 0.
// prevLabel/nextLabel/ariaLabel nhận từ ngoài để component không phụ thuộc i18n.
export default function Pagination({
  page,
  totalPages,
  onPageChange,
  prevLabel = "‹",
  nextLabel = "›",
  ariaLabel,
  className = "",
}) {
  if (!totalPages || totalPages <= 1) return null;

  const items = pageItems(page, totalPages);

  return (
    <nav className={`ui-pagination ${className}`.trim()} aria-label={ariaLabel}>
      <button
        type="button"
        className="ui-pagination__nav has-custom-focus"
        disabled={page <= 1}
        onClick={() => onPageChange?.(page - 1)}
      >
        {prevLabel}
      </button>

      <ul className="ui-pagination__list">
        {items.map((item, i) =>
          typeof item === "number" ? (
            <li key={item}>
              <button
                type="button"
                className={`ui-pagination__page has-custom-focus ${
                  item === page ? "ui-pagination__page--active" : ""
                }`.trim()}
                aria-current={item === page ? "page" : undefined}
                onClick={() => onPageChange?.(item)}
              >
                {item}
              </button>
            </li>
          ) : (
            <li key={`${item}-${i}`} className="ui-pagination__ellipsis" aria-hidden="true">
              …
            </li>
          )
        )}
      </ul>

      <button
        type="button"
        className="ui-pagination__nav has-custom-focus"
        disabled={page >= totalPages}
        onClick={() => onPageChange?.(page + 1)}
      >
        {nextLabel}
      </button>
    </nav>
  );
}
