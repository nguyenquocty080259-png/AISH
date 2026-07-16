import "./moderation-badge.css";

export default function ModerationBadge({ doc }) {
  if (doc?.visibility !== "PUBLIC" || doc?.moderationStatus !== "APPROVED") {
    return null;
  }

  const adminReviewed = doc.adminReviewedAt != null;

  return (
    <span
      className={`moderation-badge${adminReviewed ? " moderation-badge--admin" : ""}`}
    >
      {adminReviewed
        ? "Đã được kiểm duyệt bởi AI và Admin"
        : "Đã được AI kiểm duyệt"}
    </span>
  );
}
