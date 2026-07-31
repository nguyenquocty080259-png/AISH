import "./Spinner.css";

// Vòng xoay chờ. size tính bằng px, màu kế thừa currentColor nên đặt trong nút/vùng nào
// cũng tự hợp tông. label: text cho screen reader (mặc định ẩn khỏi a11y tree khi có
// text khác mô tả trạng thái, vd nút đã có aria-busy).
export default function Spinner({ size = 16, label, className = "" }) {
  return (
    <span
      className={`ui-spinner ${className}`.trim()}
      style={{ width: size, height: size }}
      role={label ? "status" : undefined}
      aria-hidden={label ? undefined : true}
    >
      {label && <span className="sr-only">{label}</span>}
    </span>
  );
}
