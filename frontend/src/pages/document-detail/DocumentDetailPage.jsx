import { useState } from "react";
import { Link } from "react-router-dom";
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
    ingested,
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
 const [previewUnlocked, setPreviewUnlocked] = useState(true);

  const submitCreateCollection = (e) => {
    e.preventDefault();
    if (!newCollectionName.trim()) return;
    handleCreateCollectionAndAdd(newCollectionName.trim());
    setNewCollectionName("");
  };

  if (loading) {
    return <div className="detail-page">Đang tải tài liệu...</div>;
  }

  if (!doc) {
    return <div className="detail-page">Không tìm thấy tài liệu.</div>;
  }

  const aiReady = doc.aiSupported !== false && doc.ingestStatus === "INGESTED";

  return (
    <div className="detail-page">
      <Link to={ROUTES.DOCUMENTS} className="detail-page__back">
        ‹ Quay lại danh sách tài liệu
      </Link>

      <div className="detail-header">
        <h1 className="detail-title">{doc.title}</h1>
        <button
          className={`detail-fav ${doc.favorited ? "detail-fav--active" : ""}`}
          onClick={handleToggleFavorite}
          aria-label="Yêu thích"
        >
          {doc.favorited ? "♥" : "♡"}
        </button>
      </div>

      <FormatBadge fileType={doc.fileType} fileName={doc.fileName} />

      <div className="detail-meta">
        <span>Người đăng: {doc.ownerName}</span>
        {doc.subjectNames?.length > 0 && <span>Môn: {doc.subjectNames.join(", ")}</span>}
        <span>Lượt tải: {doc.downloadCount ?? 0}</span>
        <span>Yêu thích: {doc.favoriteCount ?? 0}</span>
        <span>Trạng thái: {doc.visibility === "PUBLIC" ? "Công khai" : "Riêng tư"}</span>
      </div>
      <ModerationBadge doc={doc} />

      <p className="detail-desc">{doc.description}</p>

      {doc.fileUrl && (
        <div className={`detail-preview ${previewUnlocked ? "detail-preview--open" : "detail-preview--locked"}`}>
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
                      <span className="detail-citation-callout__label">Đoạn trích được chọn</span>
                      <p>&ldquo;{highlightSnippet}&rdquo;</p>
                      <span className="detail-citation-callout__hint">
                        {viewerKind === "docx" || viewerKind === "xlsx" || viewerKind === "pptx"
                          ? "Định dạng này chưa hỗ trợ tô sáng theo trang — xem nội dung bên dưới để tìm đoạn trích trên."
                          : "Định dạng này chưa hỗ trợ tô sáng trực tiếp trong trình xem — hãy mở file bên dưới để tìm đoạn trích trên."}
                      </span>
                    </div>
                  )}

                  {viewerKind === "image" &&
                    (previewBlobError ? (
                      <div className="detail-preview__load-error">Không tải được ảnh.</div>
                    ) : previewBlobUrl ? (
                      <img
                        src={previewBlobUrl}
                        alt={doc.title}
                        style={{ maxWidth: "100%", maxHeight: "80vh", borderRadius: 8 }}
                      />
                    ) : (
                      <div className="detail-preview__loading">Đang tải ảnh...</div>
                    ))}

                  {viewerKind === "pdf" &&
                    (previewBlobError ? (
                      <div className="detail-preview__load-error">Không tải được PDF.</div>
                    ) : previewBlobUrl ? (
                      <PdfViewer
                        fileUrl={previewBlobUrl}
                        initialPage={highlightPage}
                        highlightText={highlightSnippet}
                      />
                    ) : (
                      <div className="detail-preview__loading">Đang tải PDF...</div>
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
                      <div className="detail-preview__load-error">Không mở được file.</div>
                    ) : previewBlobUrl ? (
                      <a href={previewBlobUrl} target="_blank" rel="noreferrer">
                        Mở file trong tab mới
                      </a>
                    ) : (
                      <div className="detail-preview__loading">Đang chuẩn bị file...</div>
                    ))}
                </div>
              );
            })()}
          </div>

          {!previewUnlocked && (
            <div className="detail-preview__gate">
              <button
                type="button"
                className="detail-preview__gate-btn"
                onClick={() => setPreviewUnlocked(true)}
              >
                🔓 Xem đầy đủ
              </button>
              <span className="detail-preview__gate-hint">Bấm để xem rõ toàn bộ tài liệu</span>
            </div>
          )}
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
          {downloading ? "Đang tải..." : "⬇ Tải file"}
        </button>
        <button
          className="detail-btn"
          onClick={goAskAi}
          disabled={!aiReady}
        >
          {!aiReady
            ? "🤖 AI không đọc được tệp này"
            : "🤖 Hỏi AI về tài liệu này"}
        </button>
        <button className="detail-btn" onClick={openAddToCollectionModal}>
          🗂️ Thêm vào collection
        </button>

        {isLikelyOwner && (
          <>
            {false && (
            <button
              className="detail-btn"
              onClick={handleIngest}
              disabled={ingesting || doc.ingestStatus === "UNSUPPORTED_FORMAT"}
            >
              {doc.ingestStatus === "UNSUPPORTED_FORMAT"
                ? "🧠 AI không đọc được tệp này"
                : ingesting
                ? "Đang chuẩn bị..."
                : ingested
                ? "✓ Đã sẵn sàng cho AI"
                : "🧠 Chuẩn bị cho AI Chat"}
            </button>
            )}
            <button className="detail-btn" onClick={openEditModal}>
              ✏️ Sửa tài liệu
            </button>
            <button className="detail-btn" onClick={openShareModal}>
              🔗 Chia sẻ
            </button>
            <button className="detail-btn" onClick={handleToggleVisibility}>
              Đổi sang {doc.visibility === "PUBLIC" ? "riêng tư" : "công khai"}
            </button>
            <button className="detail-btn detail-btn--danger" onClick={handleDelete}>
              Xoá tài liệu
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
          Bình luận
        </button>
        <button
          type="button"
          className={`detail-tabs__tab ${activeTab === "related" ? "detail-tabs__tab--active" : ""}`}
          onClick={() => selectTab("related")}
        >
          Liên quan
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
          <p className="detail-comments__empty">Đang tải tài liệu liên quan...</p>
        ) : relatedDocs.length === 0 ? (
          <EmptyState icon="🔎" message="Chưa có tài liệu liên quan." />
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
        title="Thêm vào collection"
      >
        <p className="detail-add-collection__note">
          Thêm vào collection chỉ là tham chiếu — tài liệu vẫn ở nguyên trong My Documents.
        </p>

        {loadingCollections ? (
          <p className="detail-add-collection__loading">Đang tải danh sách collection...</p>
        ) : myCollections.length === 0 ? (
          <p className="detail-add-collection__loading">Bạn chưa có collection nào.</p>
        ) : (
          <>
            <input
              type="text"
              className="detail-add-collection__search"
              placeholder="Tìm collection..."
              value={collectionQuery}
              onChange={(e) => setCollectionQuery(e.target.value)}
            />
            {filteredCollections.length === 0 ? (
              <p className="detail-add-collection__loading">Không tìm thấy collection nào.</p>
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
                      {c.documentCount ?? 0} tài liệu
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
              {addingToCollections ? "Đang thêm..." : `Thêm (${selectedCollectionIds.length})`}
            </Button>
          </div>
        )}

        <div className="detail-add-collection__divider">Hoặc tạo collection mới</div>

        <form className="detail-add-collection__create-form" onSubmit={submitCreateCollection}>
          <input
            type="text"
            placeholder="Tên collection mới"
            value={newCollectionName}
            onChange={(e) => setNewCollectionName(e.target.value)}
          />
          <Button type="submit" variant="secondary" disabled={creatingCollection}>
            {creatingCollection ? "Đang tạo..." : "Tạo và thêm"}
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
