import { Link } from "react-router-dom";
import { useDocumentDetailPage } from "./hooks/useDocumentDetailPage";
import RatingStars from "./components/RatingStars";
import CommentSection from "./components/CommentSection";
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
        {doc.subjectName && <span>Môn: {doc.subjectName}</span>}
        <span>Lượt tải: {doc.downloadCount ?? 0}</span>
        <span>Yêu thích: {doc.favoriteCount ?? 0}</span>
        <span>Trạng thái: {doc.visibility === "PUBLIC" ? "Công khai" : "Riêng tư"}</span>
      </div>

      <p className="detail-desc">{doc.description}</p>

      {doc.tags?.length > 0 && (
        <div className="detail-tags">
          {doc.tags.map((tag) => (
            <span key={tag} className="detail-tag">
              #{tag}
            </span>
          ))}
        </div>
      )}

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