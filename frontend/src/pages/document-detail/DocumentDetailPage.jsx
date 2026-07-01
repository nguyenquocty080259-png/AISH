import { Link } from "react-router-dom";
import { useDocumentDetailPage } from "./hooks/useDocumentDetailPage";
import RatingStars from "./components/RatingStars";
import CommentSection from "./components/CommentSection";
import PdfViewer from "./components/PdfViewer";
import { ROUTES } from "../../constants/routes";
import "./document-detail.css";

export default function DocumentDetailPage() {
  const {
    doc,
    loading,
    isLikelyOwner,
    commentText,
    setCommentText,
    posting,
    downloading,
    handleToggleFavorite,
    handleRate,
    handleAddComment,
    handleDownload,
    handleToggleVisibility,
    handleDelete,
    goAskAi,
  } = useDocumentDetailPage();

  if (loading) {
    return <div className="detail-page">Đang tải tài liệu...</div>;
  }

  if (!doc) {
    return <div className="detail-page">Không tìm thấy tài liệu.</div>;
  }

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

      {doc.fileUrl && (() => {
        const fileUrl = doc.fileUrl.startsWith("http")
          ? doc.fileUrl
          : `http://localhost:8080/uploads/${doc.fileUrl}`;
        const type = (doc.fileType || "").toLowerCase();
        const name = (doc.fileName || "").toLowerCase();
        const isImage = type.includes("image") || /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(name);
        const isPdf = type.includes("pdf") || name.endsWith(".pdf");

        return (
          <div style={{ margin: "16px 0" }}>
            {isImage && (
              <img
                src={fileUrl}
                alt={doc.title}
                style={{ maxWidth: "100%", maxHeight: "80vh", borderRadius: 8 }}
              />
            )}
            {isPdf && <PdfViewer fileUrl={fileUrl} />}
            {!isImage && !isPdf && (
              <a href={fileUrl} target="_blank" rel="noreferrer">
                Mở file trong tab mới
              </a>
            )}
          </div>
        );
      })()}

      <div className="detail-actions">
        <button
          className="detail-btn detail-btn--primary"
          onClick={handleDownload}
          disabled={downloading}
        >
          {downloading ? "Đang tải..." : "⬇ Tải file"}
        </button>
        <button className="detail-btn" onClick={goAskAi}>
          🤖 Hỏi AI về tài liệu này
        </button>

        {isLikelyOwner && (
          <>
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

      <CommentSection
        comments={doc.comments}
        commentText={commentText}
        onCommentTextChange={setCommentText}
        onSubmit={handleAddComment}
        posting={posting}
      />
    </div>
  );
}