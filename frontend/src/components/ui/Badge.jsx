import "./Badge.css";

// intent: neutral | success | warning | error | info. Use for storage badges (LOCAL/CLOUD),
// moderation/appeal statuses (APPROVED/REJECTED/PENDING), tags, etc.
export default function Badge({ intent = "neutral", className = "", children }) {
  return <span className={`ui-badge ui-badge--${intent} ${className}`.trim()}>{children}</span>;
}
