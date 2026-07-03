import "./Card.css";

// clickable: adds hover affordance (border/shadow) for cards that act as links/buttons.
export default function Card({ clickable = false, className = "", children, ...rest }) {
  const clickableClass = clickable ? "ui-card--clickable" : "";
  return (
    <div className={`ui-card ${clickableClass} ${className}`.trim()} {...rest}>
      {children}
    </div>
  );
}
