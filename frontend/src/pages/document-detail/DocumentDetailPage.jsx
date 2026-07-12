import { useState } from "react";
import { Link } from "react-router-dom";
import { useDocumentDetailPage } from "./hooks/useDocumentDetailPage";
import RatingStars from "./components/RatingStars";
import CommentSection from "./components/CommentSection";
import EditDocumentModal from "./components/EditDocumentModal";
import PdfViewer from "./components/PdfViewer";
import TextFileViewer from "./components/TextFileViewer";
import AiReadinessBadge from "./components/AiReadinessBadge";
import Modal from "../../components/ui/Modal";
import Button from "../../components/ui/Button";
import EmptyState from "../../components/ui/EmptyState";
import RecommendationCard from "../../components/recommendations/RecommendationCard";
import ReportMenu from "../../components/report/ReportMenu";
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
    commentText,
    setCommentText,
    posting,
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

      <div className="detail-meta">
        <span>Người đăng: {doc.ownerName}</span>
        {doc.subjectNames?.length > 0 && <span>Môn: {doc.subjectNames.join(", ")}</span>}
        <span>Lượt tải: {doc.downloadCount ?? 0}</span>
        <span>Yêu thích: {doc.favoriteCount ?? 0}</span>
        <span>Trạng thái: {doc.visibility === "PUBLIC" ? "Công khai" : "Riêng tư"}</span>
      </div>

      <p className="detail-desc">{doc.description}</p>

      {doc.fileUrl && (
        <div className={`detail-preview ${previewUnlocked ? "detail-preview--open" : "detail-preview--locked"}`}>
          <div className="detail-preview__content">
      {(() => {
        const fileUrl = doc.fileUrl.startsWith("http")
          ? doc.fileUrl
          : `http://localhost:8080/uploads/${doc.fileUrl}`;
        const type = (doc.fileType || "").toLowerCase();
        const name = (doc.fileName || "").toLowerCase();
        const isImage = type.includes("image") || /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(name);
        const isPdf = type.includes("pdf") || name.endsWith(".pdf");
        // Cùng tiêu chí với backend (DocEmbeddingServiceImpl.resolveIngestFormat) cho nhánh TXT.
        const isTxt = type === "text/plain" || name.endsWith(".txt");

        return (
          <div style={{ margin: "16px 0" }}>
            {isImage && (
              <img
                src={fileUrl}
                alt={doc.title}
                style={{ maxWidth: "100%", maxHeight: "80vh", borderRadius: 8 }}
              />
            )}
            {isPdf && (
              <PdfViewer
                fileUrl={fileUrl}
                initialPage={highlightPage}
                highlightText={highlightSnippet}
              />
            )}
            {!isImage && !isPdf && isTxt && (
              <TextFileViewer documentId={doc.id} highlightText={highlightSnippet} />
            )}
            {!isImage && !isPdf && !isTxt && (
              <>
                {highlightSnippet && (
                  <div className="detail-citation-callout">
                    <span className="detail-citation-callout__label">Đoạn trích được chọn</span>
                    <p>&ldquo;{highlightSnippet}&rdquo;</p>
                    <span className="detail-citation-callout__hint">
                      Định dạng này chưa hỗ trợ tô sáng trực tiếp trong trình xem — hãy mở
                      file bên dưới để tìm đoạn trích trên.
                    </span>
                  </div>
                )}
                <a href={fileUrl} target="_blank" rel="noreferrer">
                  Mở file trong tab mới
                </a>
              </>
            )}
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
          <ul className="detail-add-collection__list">
            {myCollections.map((c) => (
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
    </div>
  );
}
