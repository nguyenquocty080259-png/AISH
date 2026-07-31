import "./PageHeader.css";

// title (bắt buộc), subtitle (tuỳ chọn), actions (slot node bên phải, vd nút).
// Thêm: eyebrow — nhãn nhỏ in hoa phía trên tiêu đề (tên khu vực / breadcrumb ngắn).
export default function PageHeader({ title, subtitle, eyebrow, actions, className = "" }) {
  return (
    <div className={`ui-page-header ${className}`.trim()}>
      <div className="ui-page-header__text">
        {eyebrow && <span className="ui-page-header__eyebrow">{eyebrow}</span>}
        <h1 className="ui-page-header__title">{title}</h1>
        {subtitle && <p className="ui-page-header__subtitle">{subtitle}</p>}
      </div>
      {actions && <div className="ui-page-header__actions">{actions}</div>}
    </div>
  );
}
