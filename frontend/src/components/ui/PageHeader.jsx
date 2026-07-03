import "./PageHeader.css";

// title (required), subtitle (optional), actions (optional right-side node slot, e.g. buttons).
export default function PageHeader({ title, subtitle, actions, className = "" }) {
  return (
    <div className={`ui-page-header ${className}`.trim()}>
      <div className="ui-page-header__text">
        <h1 className="ui-page-header__title">{title}</h1>
        {subtitle && <p className="ui-page-header__subtitle">{subtitle}</p>}
      </div>
      {actions && <div className="ui-page-header__actions">{actions}</div>}
    </div>
  );
}
