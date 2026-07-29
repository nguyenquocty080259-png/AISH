import { ROUTES, buildRoute } from "../constants/routes";

const DOCUMENT_LINK_TYPES = new Set([
  "DOC_APPROVED",
  "DOC_REJECTED",
  "APPEAL_APPROVED",
  "APPEAL_REJECTED",
  "COMMENT_ON_MY_DOC",
  "RATING_ON_MY_DOC",
  "DOCUMENT_SHARED",
]);

export function resolveRoute(notification, isAdmin) {
  const { type, relatedDocumentId, relatedCommentId, relatedCaseType } = notification;

  if (DOCUMENT_LINK_TYPES.has(type)) {
    return relatedDocumentId ? buildRoute(ROUTES.DOCUMENT_DETAIL, { id: relatedDocumentId }) : null;
  }

  if (type === "DOCUMENT_SCREENED" || type === "METADATA_MISMATCH") {
    if (isAdmin) return `${ROUTES.ADMIN_DOCUMENTS}?needsReview=true`;
    return relatedDocumentId ? buildRoute(ROUTES.DOCUMENT_DETAIL, { id: relatedDocumentId }) : null;
  }

  if (type === "COMMENT_UNDER_REVIEW" || type === "COMMENT_REVIEWED") {
    if (isAdmin) return `${ROUTES.ADMIN_APPEALS}?tab=comments`;
    return relatedDocumentId
      ? `${buildRoute(ROUTES.DOCUMENT_DETAIL, { id: relatedDocumentId })}?comment=${relatedCommentId}`
      : null;
  }

  if (type === "REPORT_CREATED" || type === "REPORT_RESOLVED") {
    return isAdmin ? ROUTES.ADMIN_REPORTS : ROUTES.MY_REPORTS;
  }

  if (type === "CASE_REPLY") {
    if (relatedCaseType === "REPORT") return isAdmin ? ROUTES.ADMIN_REPORTS : ROUTES.MY_REPORTS;
    if (relatedCaseType === "APPEAL") return isAdmin ? ROUTES.ADMIN_APPEALS : null;
    return null;
  }

  return null;
}
