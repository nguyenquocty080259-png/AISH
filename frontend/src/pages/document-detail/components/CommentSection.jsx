import { useState } from "react";
import ReportMenu from "../../../components/report/ReportMenu";

export default function CommentSection({
  comments,
  commentText,
  onCommentTextChange,
  onSubmit,
  posting,
  currentUserName,
  onUpdateComment,
  onDeleteComment,
}) {
  const [editingId, setEditingId] = useState(null);
  const [editText, setEditText] = useState("");

  const startEdit = (comment) => {
    setEditingId(comment.id);
    setEditText(comment.content);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditText("");
  };

  const submitEdit = async (commentId) => {
    await onUpdateComment(commentId, editText);
    cancelEdit();
  };

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
        {comments?.map((comment) => {
          // FE chỉ ẩn/hiện nút; quyền thật do backend kiểm theo user đăng nhập.
          const isMine =
            currentUserName && comment.ownerName === currentUserName;
          const isEditing = editingId === comment.id;

          return (
            <div key={comment.id} className="detail-comment">
              <p className="detail-comment__owner">{comment.ownerName}</p>

              {isEditing ? (
                <div className="detail-comment__edit">
                  <input
                    type="text"
                    value={editText}
                    onChange={(e) => setEditText(e.target.value)}
                  />
                  <button type="button" onClick={() => submitEdit(comment.id)}>
                    Lưu
                  </button>
                  <button type="button" onClick={cancelEdit}>
                    Huỷ
                  </button>
                </div>
              ) : (
                <>
                  <p className="detail-comment__content">{comment.content}</p>
                  {!isMine && <ReportMenu targetType="COMMENT" targetId={comment.id} />}
                  {isMine && (
                    <div className="detail-comment__actions">
                      <button type="button" onClick={() => startEdit(comment)}>
                        Sửa
                      </button>
                      <button
                        type="button"
                        onClick={() => onDeleteComment(comment.id)}
                      >
                        Xoá
                      </button>
                    </div>
                  )}
                </>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
