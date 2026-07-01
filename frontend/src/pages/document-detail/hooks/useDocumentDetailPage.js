import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as documentApi from "../../../api/documentApi";
import * as aiApi from "../../../api/aiApi";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES, buildRoute } from "../../../constants/routes";

export function useDocumentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showSuccess, showError } = useToast();

  const [doc, setDoc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState("");
  const [posting, setPosting] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [ingesting, setIngesting] = useState(false);
  // Chỉ phản ánh trạng thái trong phiên hiện tại (không có field ingested ở backend) -
  // reload trang sẽ mất, không phải bug.
  const [ingested, setIngested] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await documentApi.getOne(id);
      setDoc(data);
    } catch (err) {
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // GHI CHÚ: DocumentResponseDTO chỉ trả ownerName (string), không có ownerId,
  // nên đây chỉ là check tương đối ở FE để ẨN/HIỆN nút quản lý cho gọn UI.
  // Quyền thực sự vẫn do backend kiểm tra (toggle-visibility/delete) theo user đang đăng nhập.
  const isLikelyOwner = Boolean(
    user?.fullName && doc?.ownerName && user.fullName === doc.ownerName
  );

  const handleToggleFavorite = async () => {
    try {
      await documentApi.toggleFavorite(id);
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  const handleRate = async (star) => {
    try {
      await documentApi.rateDocument(id, star);
      showSuccess("Đã ghi nhận đánh giá của bạn.");
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;
    setPosting(true);
    try {
      await documentApi.addComment(id, commentText.trim());
      setCommentText("");
      await load();
    } catch (err) {
      showError(err.message);
    } finally {
      setPosting(false);
    }
  };

  const handleDownload = async () => {
    setDownloading(true);
    try {
      const res = await documentApi.downloadFile(id);
      const disposition = res.headers["content-disposition"];
      let filename = doc?.fileName || "tai-lieu";
      const match = disposition?.match(/filename="?([^"]+)"?/);
      if (match) filename = decodeURIComponent(match[1]);

      const url = window.URL.createObjectURL(res.data);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      showError(err.message || "Không thể tải file.");
    } finally {
      setDownloading(false);
    }
  };

  // Chuẩn bị tài liệu cho AI chat (RAG). Endpoint LUÔN trả 200 kể cả khi không ingest được
  // (vd file không phải PDF) - phải đọc res.status, không thể chỉ dựa vào catch().
  const handleIngest = async () => {
    setIngesting(true);
    try {
      const res = await aiApi.ingest(id);
      if (res.status === "INGESTED") {
        setIngested(true);
        showSuccess(res.message || "Đã chuẩn bị tài liệu cho AI chat.");
      } else {
        showError(res.message || "Không thể chuẩn bị tài liệu này cho AI.");
      }
    } catch (err) {
      showError(err.message || "Không thể chuẩn bị tài liệu cho AI, vui lòng thử lại.");
    } finally {
      setIngesting(false);
    }
  };

  const handleToggleVisibility = async () => {
    try {
      await documentApi.toggleVisibility(id);
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  const handleDelete = async () => {
    try {
      await documentApi.deleteDocument(id);
      showSuccess("Đã xoá tài liệu.");
      navigate(ROUTES.DOCUMENTS);
    } catch (err) {
      showError(err.message);
    }
  };

  const goAskAi = () => {
    navigate(`${ROUTES.AI_CHAT}?documentId=${id}`);
  };

  return {
    doc,
    loading,
    isLikelyOwner,
    commentText,
    setCommentText,
    posting,
    downloading,
    ingesting,
    ingested,
    handleToggleFavorite,
    handleRate,
    handleAddComment,
    handleDownload,
    handleIngest,
    handleToggleVisibility,
    handleDelete,
    goAskAi,
    documentsRoute: buildRoute(ROUTES.DOCUMENTS),
  };
}
