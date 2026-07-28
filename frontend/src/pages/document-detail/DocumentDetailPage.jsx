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
    return <div className="detail-page">{t("docDetail.loadingDoc")}</div>;
  }

  if (!doc) {
    return <div className="detail-page">{t("docDetail.notFound")}</div>;
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

      <div className="detail-header">
        <h1 className="detail-title">{doc.title}</h1>
        <button
          className={`detail-fav ${doc.favorited ? "detail-fav--active" : ""}`}
          onClick={handleToggleFavorite}
          aria-label={t("docDetail.favorite")}
        >
          {doc.favorited ? "♥" : "♡"}
        </button>
      </div>

      <FormatBadge fileType={doc.fileType} fileName={doc.fileName} />

      <div className="detail-meta">
        <span>{t("docDetail.metaOwner", { name: doc.ownerName })}</span>
        {doc.subjectNames?.length > 0 && <span>{t("docDetail.metaSubject", { names: doc.subjectNames.join(", ") })}</span>}
        <span>{t("docDetail.metaDownloads", { count: doc.downloadCount ?? 0 })}</span>
        <span>{t("docDetail.metaFavorites", { count: doc.favoriteCount ?? 0 })}</span>
        <span>{t("docDetail.metaStatus", { status: doc.visibility === "PUBLIC" ? t("docDetail.visibilityPublic") : t("docDetail.visibilityPrivate") })}</span>
      </div>
      <ModerationBadge doc={doc} />

      <p className="detail-desc">{doc.description}</p>

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
                <div style={{ margin: "16px 0" }}>
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
                        src={previewBlobUrl}
                        alt={doc.title}
                        style={{ maxWidth: "100%", maxHeight: "80vh", borderRadius: 8 }}
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

      <AiReadinessBadge
        aiSupported={doc.aiSupported}
        ingestStatus={doc.ingestStatus}
        isOwner={isLikelyOwner}
        ingesting={ingesting}
        onIngest={handleIngest}
      />
      <div className="detail-actions">
        <ReportMenu targetType="DOCUMENT" targetId={doc.id} />
        <button
          className="detail-btn detail-btn--primary"
          onClick={handleDownload}
          disabled={downloading}
        >
          {downloading ? t("docDetail.downloading") : t("docDetail.download")}
        </button>
        <button
          className="detail-btn"
          onClick={goAskAi}
          disabled={!aiReady}
        >
          {aiReady
            ? t("docDetail.askAi")
            : aiSupported
            ? t("docDetail.aiNotReady")
            : t("docDetail.aiCantRead")}
        </button>
        <button className="detail-btn" onClick={openAddToCollectionModal}>
          {t("docDetail.addToCollection")}
        </button>

        {isLikelyOwner && (
          <>
            <button className="detail-btn" onClick={openEditModal}>
              {t("docDetail.edit")}
            </button>
            <button className="detail-btn" onClick={openShareModal}>
              {t("docDetail.share")}
            </button>
            <button className="detail-btn" onClick={handleToggleVisibility}>
              {t("docDetail.changeVisibility", { target: doc.visibility === "PUBLIC" ? t("docDetail.toPrivate") : t("docDetail.toPublic") })}
            </button>
            <button className="detail-btn detail-btn--danger" onClick={handleDelete}>
              {t("docDetail.deleteDoc")}
            </button>
          </>
        )}
      </div>

      <RatingStars average={doc.averageRating} onRate={handleRate} />

      <div className="detail-tabs">
        <button
          type="button"
          className={`detail-tabs__tab ${activeTab === "comments" ? "detail-tabs__tab--active" : ""}`}
          onClick={() => selectTab("comments")}
        >
          {t("docDetail.tabComments")}
        </button>
        <button
          type="button"
          className={`detail-tabs__tab ${activeTab === "related" ? "detail-tabs__tab--active" : ""}`}
          onClick={() => selectTab("related")}
        >
          {t("docDetail.tabRelated")}
        </button>
      </div>

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
            <input
              type="text"
              className="detail-add-collection__search"
              placeholder={t("docDetail.addColSearch")}
              value={collectionQuery}
              onChange={(e) => setCollectionQuery(e.target.value)}
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
              disabled={addingToCollections || selectedCollectionIds.length === 0}
            >
              {addingToCollections ? t("docDetail.adding") : t("docDetail.addWithCount", { count: selectedCollectionIds.length })}
            </Button>
          </div>
        )}

        <div className="detail-add-collection__divider">{t("docDetail.orCreateNew")}</div>

        <form className="detail-add-collection__create-form" onSubmit={submitCreateCollection}>
          <input
            type="text"
            placeholder={t("docDetail.newColName")}
            value={newCollectionName}
            onChange={(e) => setNewCollectionName(e.target.value)}
          />
          <Button type="submit" variant="secondary" disabled={creatingCollection}>
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
