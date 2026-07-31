import { useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useDocumentDetailPage, resolveViewerKind } from "./hooks/useDocumentDetailPage";
import RatingStars from "./components/RatingStars";
import CommentSection from "./components/CommentSection";
import EditDocumentModal from "./components/EditDocumentModal";
import ShareModal from "./components/ShareModal";
import PdfViewer from "./components/PdfViewer";
import TextFileViewer from "./components/TextFileViewer";
import DocxViewer from "./components/DocxViewer";
import XlsxViewer from "./components/XlsxViewer";
import ExtractedTextViewer from "./components/ExtractedTextViewer";
import AiReadinessBadge from "./components/AiReadinessBadge";
import Modal from "../../components/ui/Modal";
import Button from "../../components/ui/Button";
import EmptyState from "../../components/ui/EmptyState";
import FormatBadge from "../../components/ui/FormatBadge";
import Tabs from "../../components/ui/Tabs";
import Skeleton, { SkeletonText } from "../../components/ui/Skeleton";
import { Input } from "../../components/ui/Field";
import { ModerationBadge as ModerationStatusBadge, VisibilityBadge } from "../../components/ui/StatusBadge";
import RecommendationCard from "../../components/recommendations/RecommendationCard";
import ReportMenu from "../../components/report/ReportMenu";
import ModerationBadge from "../document/components/ModerationBadge";
import { ROUTES } from "../../constants/routes";
import "./document-detail.css";

export default function DocumentDetailPage() {
  const { t } = useTranslation();
  const {
    doc,
    loading,
    isLikelyOwner,
    currentUserName,
    highlightPage,
    highlightSnippet,
    previewBlobUrl,
    previewBlobError,
    commentText,
    setCommentText,
    posting,
    blockedComment,
    dismissBlockedComment,
    disputeBlockedComment,
    downloading,
    ingesting,
    handleToggleFavorite,
    handleRate,
    handleAddComment,
    handleUpdateComment,
    handleDeleteComment,
    handleUpdateDocument,
    editModalOpen,
    editSubmitting,
    subjects,
    openEditModal,
    closeEditModal,
    shareModalOpen,
    sharing,
    openShareModal,
    closeShareModal,
    handleShare,
    shareRecipients,
    loadingRecipients,
    handleRevokeShare,
    documentDetailPath,
    handleDownload,
    handleIngest,
    handleToggleVisibility,
    handleDelete,
    isAdmin,
    adminReviewing,
    handleAdminReview,
    goAskAi,
    activeTab,
    selectTab,
    relatedDocs,
    loadingRelated,
    goToDocument,
    addToCollectionModalOpen,
    myCollections,
    filteredCollections,
    collectionQuery,
    setCollectionQuery,
    loadingCollections,
    selectedCollectionIds,
    addingToCollections,
    creatingCollection,
    openAddToCollectionModal,
    closeAddToCollectionModal,
    toggleSelectCollection,
    handleAddToCollections,
    handleCreateCollectionAndAdd,
  } = useDocumentDetailPage();

  const [newCollectionName, setNewCollectionName] = useState("");

  const submitCreateCollection = (e) => {
    e.preventDefault();
    if (!newCollectionName.trim()) return;
    handleCreateCollectionAndAdd(newCollectionName.trim());
    setNewCollectionName("");
  };

  if (loading) {
    return (
      <div className="detail-page detail-page--centered" aria-busy="true" aria-label={t("docDetail.loadingDoc")}>
        <Skeleton variant="title" width="55%" />
        <div className="detail-skeleton">
          <SkeletonText lines={2} />
          <Skeleton variant="block" />
          <SkeletonText lines={3} />
        </div>
      </div>
    );
  }

  if (!doc) {
    return (
      <div className="detail-page detail-page--centered">
        <EmptyState
          tone="danger"
          icon="⚠️"
          title={t("docDetail.notFound")}
          action={
            <Link to={ROUTES.DOCUMENTS} className="ui-btn ui-btn--secondary ui-btn--md has-custom-focus">
              {t("docDetail.back")}
            </Link>
          }
        />
      </div>
    );
  }

  // Hai lý do rất khác nhau khiến nút Hỏi AI bị tắt: định dạng AI không đọc được (bế tắc thật,
  // chủ tài liệu cũng không làm gì được) và tệp hỗ trợ nhưng chưa nạp (chỉ cần bấm nạp ở
  // AiReadinessBadge). Gộp chung một nhãn khiến tài liệu vừa upload bị hiểu nhầm là hỏng.
  const aiSupported = doc.aiSupported !== false;
  const aiReady = aiSupported && doc.ingestStatus === "INGESTED";

  return (
    <div className="detail-page">
      <Link to={ROUTES.DOCUMENTS} className="detail-page__back">
        {t("docDetail.back")}
      </Link>

      <div className="detail-layout">
        <div className="detail-main">

          <div className="detail-header">
            <div>
              <h1 className="detail-title">{doc.title}</h1>
              <div className="detail-badges">
                <FormatBadge fileType={doc.fileType} fileName={doc.fileName} />
                <VisibilityBadge visibility={doc.visibility} size="sm" />
                <ModerationStatusBadge status={doc.moderationStatus} size="sm" />
                <ModerationBadge doc={doc} />
              </div>
            </div>
            <button
              className={`detail-fav has-custom-focus ${doc.favorited ? "detail-fav--active" : ""}`.trim()}
              onClick={handleToggleFavorite}
              aria-pressed={!!doc.favorited}
              aria-label={t("docDetail.favorite")}
            >
              <span aria-hidden="true">{doc.favorited ? "♥" : "♡"}</span>
            </button>
          </div>

          {/* Panel xem xét của Admin: nổi bật khi tài liệu đang chờ duyệt, còn với tài liệu đã có
              quyết định thì chỉ nhắc trạng thái nhưng vẫn cho Admin đổi ý (duyệt lại / gỡ công khai). */}
          {isAdmin && (
            <div
              className={`detail-admin-review${
                doc.moderationStatus === "ADMIN_PENDING" ? " detail-admin-review--pending" : ""
              }`}
            >
              <div className="detail-admin-review__info">
                <span className="detail-admin-review__title">{t("docDetail.adminReview.title")}</span>
                <span className="detail-admin-review__hint">
                  {doc.moderationStatus === "ADMIN_PENDING"
                    ? doc.aiScreenOutcome === "FLAG"
                      ? t("docDetail.adminReview.pendingFlaggedHint")
                      : t("docDetail.adminReview.pendingHint")
                    : doc.moderationStatus === "APPROVED"
                    ? t("docDetail.adminReview.approvedHint")
                    : doc.moderationStatus === "REJECTED"
                    ? t("docDetail.adminReview.rejectedHint")
                    : t("docDetail.adminReview.noRequestHint")}
                </span>
                {doc.moderationReason && (
                  <span className="detail-admin-review__reason">
                    {t("docDetail.adminReview.reason", { reason: doc.moderationReason })}
                  </span>
                )}
              </div>
              <div className="detail-admin-review__actions">
                <Button
                  onClick={() => handleAdminReview("approve")}
                  loading={adminReviewing === "approve"}
                  disabled={adminReviewing !== null}
                >
                  {adminReviewing === "approve"
                    ? t("docDetail.adminReview.approving")
                    : t("docDetail.adminReview.approve")}
                </Button>
                <Button
                  variant="danger"
                  onClick={() => handleAdminReview("reject")}
                  loading={adminReviewing === "reject"}
                  disabled={adminReviewing !== null}
                >
                  {adminReviewing === "reject"
                    ? t("docDetail.adminReview.rejecting")
                    : t("docDetail.adminReview.reject")}
                </Button>
              </div>
            </div>
          )}

          {doc.description && <p className="detail-desc">{doc.description}</p>}

          {doc.fileUrl && (
            <div className="detail-preview detail-preview--open">
              <div className="detail-preview__content">
                {(() => {
                  const viewerKind = resolveViewerKind(doc.fileType, doc.fileName);
                  // PDF/TXT tô sáng đoạn trích ngay trong nội dung (xem PdfViewer/TextFileViewer);
                  // ảnh không áp dụng khái niệm trích dẫn văn bản. Các định dạng còn lại (DOCX/
                  // XLSX/PPTX/khác) không có trang thật (page=null) nên chỉ hiển thị callout phía
                  // trên, không tô sáng/nhảy trang được (xem RULES trong đặc tả A3).
                  const CALLOUT_KINDS = new Set(["docx", "xlsx", "pptx", "other"]);
                  const showCitationCallout = highlightSnippet && CALLOUT_KINDS.has(viewerKind);

                  return (
                    <div className="detail-preview__inner">
                      {showCitationCallout && (
                        <div className="detail-citation-callout">
                          <span className="detail-citation-callout__label">{t("docDetail.citationLabel")}</span>
                          <p>&ldquo;{highlightSnippet}&rdquo;</p>
                          <span className="detail-citation-callout__hint">
                            {viewerKind === "docx" || viewerKind === "xlsx" || viewerKind === "pptx"
                              ? t("docDetail.citationHintPaged")
                              : t("docDetail.citationHintViewer")}
                          </span>
                        </div>
                      )}

                      {viewerKind === "image" &&
                        (previewBlobError ? (
                          <div className="detail-preview__load-error">{t("docDetail.imgLoadError")}</div>
                        ) : previewBlobUrl ? (
                          <img
                            className="detail-preview__image"
                            src={previewBlobUrl}
                            alt={doc.title}
                          />
                        ) : (
                          <div className="detail-preview__loading">{t("docDetail.imgLoading")}</div>
                        ))}

                      {viewerKind === "pdf" &&
                        (previewBlobError ? (
                          <div className="detail-preview__load-error">{t("docDetail.pdfLoadError")}</div>
                        ) : previewBlobUrl ? (
                          <PdfViewer
                            fileUrl={previewBlobUrl}
                            initialPage={highlightPage}
                            highlightText={highlightSnippet}
                          />
                        ) : (
                          <div className="detail-preview__loading">{t("docDetail.pdfLoading")}</div>
                        ))}

                      {viewerKind === "txt" && (
                        <TextFileViewer documentId={doc.id} highlightText={highlightSnippet} />
                      )}

                      {viewerKind === "docx" && (
                        <DocxViewer documentId={doc.id} highlightText={highlightSnippet} />
                      )}

                      {viewerKind === "xlsx" && <XlsxViewer documentId={doc.id} />}

                      {viewerKind === "pptx" && (
                        <ExtractedTextViewer documentId={doc.id} highlightText={highlightSnippet} />
                      )}

                      {viewerKind === "other" &&
                        (previewBlobError ? (
                          <div className="detail-preview__load-error">{t("docDetail.fileOpenError")}</div>
                        ) : previewBlobUrl ? (
                          <a href={previewBlobUrl} target="_blank" rel="noreferrer">
                            {t("docDetail.openInNewTab")}
                          </a>
                        ) : (
                          <div className="detail-preview__loading">{t("docDetail.filePreparing")}</div>
                        ))}
                    </div>
                  );
                })()}
              </div>
            </div>
          )}

          <Tabs
            className="detail-tabs"
            items={[
              { value: "comments", label: t("docDetail.tabComments") },
              { value: "related", label: t("docDetail.tabRelated") },
            ]}
            value={activeTab}
            onChange={selectTab}
            ariaLabel={t("docDetail.tabComments")}
          />

          {activeTab === "comments" && (
            <CommentSection
              comments={doc.comments}
              commentText={commentText}
              onCommentTextChange={setCommentText}
              onSubmit={handleAddComment}
              posting={posting}
              currentUserName={currentUserName}
              onUpdateComment={handleUpdateComment}
              onDeleteComment={handleDeleteComment}
              blockedComment={blockedComment}
              onDismissBlocked={dismissBlockedComment}
              onDisputeBlocked={disputeBlockedComment}
            />
          )}

          {activeTab === "related" &&
            (loadingRelated ? (
              <p className="detail-comments__empty">{t("docDetail.loadingRelated")}</p>
            ) : relatedDocs.length === 0 ? (
              <EmptyState icon="🔎" message={t("docDetail.noRelated")} />
            ) : (
              <div className="detail-related-grid">
                {relatedDocs.map((item) => (
                  <RecommendationCard
                    key={item.documentId}
                    item={item}
                    onClick={() => goToDocument(item.documentId)}
                  />
                ))}
              </div>
            ))}

        </div>

        {/* ===== Cột phải: hành động + thông tin nhanh (dính khi cuộn ở màn rộng) ===== */}
        <aside className="detail-side">
          <div className="detail-side__card">
            <AiReadinessBadge
              aiSupported={doc.aiSupported}
              ingestStatus={doc.ingestStatus}
              isOwner={isLikelyOwner}
              ingesting={ingesting}
              onIngest={handleIngest}
            />

            <div className="detail-actions">
              <Button block onClick={handleDownload} loading={downloading}>
                {downloading ? t("docDetail.downloading") : t("docDetail.download")}
              </Button>

              <Button variant="secondary" block onClick={goAskAi} disabled={!aiReady}>
                {aiReady
                  ? t("docDetail.askAi")
                  : aiSupported
                  ? t("docDetail.aiNotReady")
                  : t("docDetail.aiCantRead")}
              </Button>

              <div className="detail-actions__row">
                <Button variant="secondary" onClick={openAddToCollectionModal}>
                  {t("docDetail.addToCollection")}
                </Button>
                <ReportMenu targetType="DOCUMENT" targetId={doc.id} />
              </div>

              {isLikelyOwner && (
                <>
                  <div className="detail-actions__divider" />
                  <div className="detail-actions__row">
                    <Button variant="ghost" onClick={openEditModal}>{t("docDetail.edit")}</Button>
                    <Button variant="ghost" onClick={openShareModal}>{t("docDetail.share")}</Button>
                  </div>
                  <Button variant="ghost" block onClick={handleToggleVisibility}>
                    {t("docDetail.changeVisibility", { target: doc.visibility === "PUBLIC" ? t("docDetail.toPrivate") : t("docDetail.toPublic") })}
                  </Button>
                  <Button variant="danger" block onClick={handleDelete}>
                    {t("docDetail.deleteDoc")}
                  </Button>
                </>
              )}
            </div>
          </div>

          <div className="detail-side__card">
            <RatingStars average={doc.averageRating} onRate={handleRate} />
          </div>

          <div className="detail-side__card">
            <span className="detail-side__title">{t("docDetail.infoTitle")}</span>
            <div className="detail-meta">
              <div className="detail-meta__row">
                <span className="detail-meta__label">{t("docDetail.infoOwner")}</span>
                <span className="detail-meta__value">{doc.ownerName}</span>
              </div>
              {doc.subjectNames?.length > 0 && (
                <div className="detail-meta__row">
                  <span className="detail-meta__label">{t("docDetail.infoSubject")}</span>
                  <span className="detail-meta__value">{doc.subjectNames.join(", ")}</span>
                </div>
              )}
              <div className="detail-meta__row">
                <span className="detail-meta__label">{t("docDetail.infoDownloads")}</span>
                <span className="detail-meta__value">{doc.downloadCount ?? 0}</span>
              </div>
              <div className="detail-meta__row">
                <span className="detail-meta__label">{t("docDetail.infoFavorites")}</span>
                <span className="detail-meta__value">{doc.favoriteCount ?? 0}</span>
              </div>
            </div>
          </div>
        </aside>
      </div>

      <Modal
        open={addToCollectionModalOpen}
        onClose={closeAddToCollectionModal}
        title={t("docDetail.addColTitle")}
      >
        <p className="detail-add-collection__note">
          {t("docDetail.addColNote")}
        </p>

        {loadingCollections ? (
          <p className="detail-add-collection__loading">{t("docDetail.addColLoading")}</p>
        ) : myCollections.length === 0 ? (
          <p className="detail-add-collection__loading">{t("docDetail.addColEmpty")}</p>
        ) : (
          <>
            <Input
              type="search"
              placeholder={t("docDetail.addColSearch")}
              aria-label={t("docDetail.addColSearch")}
              value={collectionQuery}
              onChange={(e) => setCollectionQuery(e.target.value)}
              fieldClassName="detail-add-collection__search"
            />
            {filteredCollections.length === 0 ? (
              <p className="detail-add-collection__loading">{t("docDetail.addColNoMatch")}</p>
            ) : (
              <ul className="detail-add-collection__list">
                {filteredCollections.map((c) => (
                  <li key={c.id} className="detail-add-collection__item">
                    <label>
                      <input
                        type="checkbox"
                        checked={selectedCollectionIds.includes(c.id)}
                        onChange={() => toggleSelectCollection(c.id)}
                      />
                      {c.name}
                    </label>
                    <span className="detail-add-collection__count">
                      {t("docDetail.addColDocCount", { count: c.documentCount ?? 0 })}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </>
        )}

        {myCollections.length > 0 && (
          <div className="detail-add-collection__actions">
            <Button
              variant="primary"
              onClick={handleAddToCollections}
              loading={addingToCollections}
              disabled={selectedCollectionIds.length === 0}
            >
              {addingToCollections ? t("docDetail.adding") : t("docDetail.addWithCount", { count: selectedCollectionIds.length })}
            </Button>
          </div>
        )}

        <div className="detail-add-collection__divider">{t("docDetail.orCreateNew")}</div>

        <form className="detail-add-collection__create-form" onSubmit={submitCreateCollection}>
          <Input
            type="text"
            placeholder={t("docDetail.newColName")}
            aria-label={t("docDetail.newColName")}
            value={newCollectionName}
            onChange={(e) => setNewCollectionName(e.target.value)}
            fieldClassName="detail-add-collection__create-input"
          />
          <Button type="submit" variant="secondary" loading={creatingCollection}>
            {creatingCollection ? t("docDetail.creating") : t("docDetail.createAndAdd")}
          </Button>
        </form>
      </Modal>

      {editModalOpen && (
        <EditDocumentModal
          open
          doc={doc}
          subjects={subjects}
          submitting={editSubmitting}
          onClose={closeEditModal}
          onSubmit={handleUpdateDocument}
        />
      )}

      {shareModalOpen && (
        <ShareModal
          open
          sharing={sharing}
          documentDetailPath={documentDetailPath}
          onShare={handleShare}
          onClose={closeShareModal}
          recipients={shareRecipients}
          loadingRecipients={loadingRecipients}
          onRevoke={handleRevokeShare}
        />
      )}
    </div>
  );
}
