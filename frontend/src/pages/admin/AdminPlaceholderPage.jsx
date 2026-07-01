// Placeholder — chỉ để chứng minh AdminRoute hoạt động đúng (guard theo role).
// Thay bằng trang Admin thật (stats/appeals queue/document oversight) ở task sau.
export default function AdminPlaceholderPage() {
  return (
    <div className="container" style={{ paddingTop: "var(--spacing-xl)" }}>
      <h1 style={{ fontSize: "var(--headline-lg)", color: "var(--color-text)" }}>
        Trang quản trị
      </h1>
      <p style={{ color: "var(--color-text-secondary)" }}>
        Khu vực dành cho Admin — đang được xây dựng.
      </p>
    </div>
  );
}
