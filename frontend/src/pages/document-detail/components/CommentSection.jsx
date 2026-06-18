export default function CommentSection({
  comments,
  commentText,
  onCommentTextChange,
  onSubmit,
  posting,
}) {
  return (
    <div className="detail-comments">
      <h3 className="detail-comments__title">Bình luận ({comments?.length ?? 0})</h3>

      <form className="detail-comments__form" onSubmit={onSubmit}>
        <input
          type="text"
          value={commentText}
          onChange={(e) => onCommentTextChange(e.target.value)}
          placeholder="Viết bình luận..."
        />
        <button type="submit" disabled={posting}>
          {posting ? "Đang gửi..." : "Gửi"}
        </button>
      </form>

      <div className="detail-comments__list">
        {comments?.length === 0 && (
          <p className="detail-comments__empty">Chưa có bình luận nào.</p>
        )}
        {comments?.map((comment) => (
          <div key={comment.id} className="detail-comment">
            <p className="detail-comment__owner">{comment.ownerName}</p>
            <p className="detail-comment__content">{comment.content}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
