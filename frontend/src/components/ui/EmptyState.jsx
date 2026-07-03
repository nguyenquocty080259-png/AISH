import Button from "./Button";
import "./EmptyState.css";

// icon: optional emoji/node. message: required. actionLabel+onAction: optional CTA button.
export default function EmptyState({ icon, message, actionLabel, onAction, className = "" }) {
  return (
    <div className={`ui-empty-state ${className}`.trim()}>
      {icon && <div className="ui-empty-state__icon">{icon}</div>}
      <p className="ui-empty-state__message">{message}</p>
      {actionLabel && onAction && (
        <Button variant="secondary" onClick={onAction} className="ui-empty-state__action">
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
