import "./Skeleton.css";

// Khối xám nhấp nháy dùng khi đang tải. variant: line | title | block | circle.
// Truyền width/height tuỳ ý (số -> px, chuỗi -> giữ nguyên: "60%", "12rem"...).
export default function Skeleton({
  variant = "line",
  width,
  height,
  radius,
  className = "",
  style,
  ...rest
}) {
  return (
    <span
      className={`ui-skeleton ui-skeleton--${variant} ${className}`.trim()}
      style={{ width, height, borderRadius: radius, ...style }}
      aria-hidden="true"
      {...rest}
    />
  );
}

// Nhiều dòng text giả — dùng cho đoạn mô tả đang tải.
export function SkeletonText({ lines = 3, className = "" }) {
  return (
    <span className={`ui-skeleton-text ${className}`.trim()} aria-hidden="true">
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton key={i} variant="line" width={i === lines - 1 ? "60%" : "100%"} />
      ))}
    </span>
  );
}

// Khung card giả cho lưới tài liệu/bộ sưu tập đang tải.
export function SkeletonCard({ className = "" }) {
  return (
    <div className={`ui-skeleton-card ${className}`.trim()} aria-hidden="true">
      <Skeleton variant="block" />
      <div className="ui-skeleton-card__body">
        <Skeleton variant="title" width="75%" />
        <Skeleton variant="line" width="45%" />
      </div>
    </div>
  );
}
