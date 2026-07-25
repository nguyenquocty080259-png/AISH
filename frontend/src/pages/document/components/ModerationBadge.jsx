import { useTranslation } from "react-i18next";
import "./moderation-badge.css";

export default function ModerationBadge({ doc }) {
  const { t } = useTranslation();
  if (doc?.visibility !== "PUBLIC" || doc?.moderationStatus !== "APPROVED") {
    return null;
  }

  const adminReviewed = doc.adminReviewedAt != null;

  return (
    <span
      className={`moderation-badge${adminReviewed ? " moderation-badge--admin" : ""}`}
    >
      {adminReviewed
        ? t("documents.moderationAiAdmin")
        : t("documents.moderationAi")}
    </span>
  );
}
