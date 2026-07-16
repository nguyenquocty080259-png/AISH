import { useState } from "react";
import ReportMenu from "../../../components/report/ReportMenu";
import Modal from "../../../components/ui/Modal";
import Button from "../../../components/ui/Button";

export default function CommentSection({
  comments,
  commentText,
  onCommentTextChange,
  onSubmit,
  posting,
  currentUserName,
  onUpdateComment,
  onDeleteComment,
  blockedComment,
  onDismissBlocked,
  onDisputeBlocked,
}) {
  const [editingId, setEditingId] = useState(null);
  const [editText, setEditText] = useState("");
  const [disputing, setDisputing] = useState(false);
  const [disputeNote, setDisputeNote] = useState("");

  const startEdit = (comment) => {
    setEditingId(comment.id);
    setEditText(comment.content);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditText("");
  };

  const submitEdit = async (commentId) => {
    if (await onUpdateComment(commentId, editText)) cancelEdit();
  };

  const closeBlocked = () => {
    setDisputing(false);
    setDisputeNote("");
    onDismissBlocked();
  };

  const submitDispute = async (event) => {
    event.preventDefault();
    if (!disputeNote.trim()) return;
    if (await onDisputeBlocked(disputeNote)) {
      setDisputing(false);
      setDisputeNote("");
      if (blockedComment?.mode === "update") cancelEdit();
    }
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
          const isPending = comment.status === "PENDING_REVIEW";

          return (
            <div key={comment.id} className={`detail-comment${isPending ? " detail-comment--pending" : ""}`}>
              <p className="detail-comment__owner">{comment.ownerName}</p>
              {isPending && <span className="detail-comment__pending-tag">Đang chờ duyệt</span>}

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
                  <p className={`detail-comment__content${isPending ? " detail-comment__content--blurred" : ""}`}>
                    {comment.content}
                  </p>
                  {!isMine && !isPending && <ReportMenu targetType="COMMENT" targetId={comment.id} />}
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

      <Modal open={!!blockedComment} onClose={closeBlocked} title="Bình luận chưa thể đăng">
        {!disputing ? (
          <>
            <p className="detail-comments__blocked-reason">
              {blockedComment?.reason || "Bình luận có nội dung cần được xem xét."}
            </p>
            <div className="detail-comments__modal-actions">
              <Button variant="secondary" onClick={closeBlocked}>Đã hiểu</Button>
              <Button variant="primary" onClick={() => setDisputing(true)}>Khiếu nại</Button>
            </div>
          </>
        ) : (
          <form className="detail-comments__dispute-form" onSubmit={submitDispute}>
            <label>
              Lý do khiếu nại
              <textarea
                autoFocus
                required
                rows={4}
                value={disputeNote}
                onChange={(event) => setDisputeNote(event.target.value)}
                placeholder="Giải thích vì sao bình luận này nên được xem xét lại..."
              />
            </label>
            <div className="detail-comments__modal-actions">
              <Button type="button" variant="secondary" disabled={posting} onClick={() => setDisputing(false)}>
                Quay lại
              </Button>
              <Button type="submit" variant="primary" disabled={posting || !disputeNote.trim()}>
                {posting ? "Đang gửi..." : "Gửi khiếu nại"}
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
