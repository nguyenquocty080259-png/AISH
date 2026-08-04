import { useState } from "react";
import { useTranslation } from "react-i18next";
import ReportMenu from "../../../components/report/ReportMenu";
import Modal from "../../../components/ui/Modal";
import Button from "../../../components/ui/Button";

// Khu vực BÌNH LUẬN: danh sách, form thêm mới, sửa/xoá bình luận của mình, và modal xử lý khi
// bình luận bị chặn (lọc từ khoá) — cho phép hiểu lý do và gửi khiếu nại để chờ Admin duyệt.
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
  const { t } = useTranslation();
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
      <h3 className="detail-comments__title">{t("docDetail.comments.title", { count: comments?.length ?? 0 })}</h3>

      <form className="detail-comments__form" onSubmit={onSubmit}>
        <input
          type="text"
          value={commentText}
          onChange={(e) => onCommentTextChange(e.target.value)}
          placeholder={t("docDetail.comments.placeholder")}
        />
        <button type="submit" disabled={posting}>
          {posting ? t("docDetail.comments.sending") : t("docDetail.comments.send")}
        </button>
      </form>

      <div className="detail-comments__list">
        {comments?.length === 0 && (
          <p className="detail-comments__empty">{t("docDetail.comments.empty")}</p>
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
              {isPending && <span className="detail-comment__pending-tag">{t("docDetail.comments.pending")}</span>}

              {isEditing ? (
                <div className="detail-comment__edit">
                  <input
                    type="text"
                    value={editText}
                    onChange={(e) => setEditText(e.target.value)}
                  />
                  <button type="button" onClick={() => submitEdit(comment.id)}>
                    {t("docDetail.comments.save")}
                  </button>
                  <button type="button" onClick={cancelEdit}>
                    {t("docDetail.comments.cancel")}
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
                        {t("docDetail.comments.edit")}
                      </button>
                      <button
                        type="button"
                        onClick={() => onDeleteComment(comment.id)}
                      >
                        {t("docDetail.comments.delete")}
                      </button>
                    </div>
                  )}
                </>
              )}
            </div>
          );
        })}
      </div>

      <Modal open={!!blockedComment} onClose={closeBlocked} title={t("docDetail.comments.blockedTitle")}>
        {!disputing ? (
          <>
            <p className="detail-comments__blocked-reason">
              {blockedComment?.reason || t("docDetail.comments.blockedDefault")}
            </p>
            <div className="detail-comments__modal-actions">
              <Button variant="secondary" onClick={closeBlocked}>{t("docDetail.comments.understood")}</Button>
              <Button variant="primary" onClick={() => setDisputing(true)}>{t("docDetail.comments.dispute")}</Button>
            </div>
          </>
        ) : (
          <form className="detail-comments__dispute-form" onSubmit={submitDispute}>
            <label>
              {t("docDetail.comments.disputeReason")}
              <textarea
                autoFocus
                required
                rows={4}
                value={disputeNote}
                onChange={(event) => setDisputeNote(event.target.value)}
                placeholder={t("docDetail.comments.disputePlaceholder")}
              />
            </label>
            <div className="detail-comments__modal-actions">
              <Button type="button" variant="secondary" disabled={posting} onClick={() => setDisputing(false)}>
                {t("docDetail.comments.goBack")}
              </Button>
              <Button type="submit" variant="primary" disabled={posting || !disputeNote.trim()}>
                {posting ? t("docDetail.comments.sending") : t("docDetail.comments.sendDispute")}
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
