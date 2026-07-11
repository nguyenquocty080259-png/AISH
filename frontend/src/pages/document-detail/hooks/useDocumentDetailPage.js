import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import * as documentApi from "../../../api/documentApi";
import * as aiApi from "../../../api/aiApi";
import * as collectionApi from "../../../api/collectionApi";
import * as subjectApi from "../../../api/subjectApi";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES, buildRoute } from "../../../constants/routes";

export function useDocumentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showSuccess, showError } = useToast();
  const [searchParams] = useSearchParams();

  // Cầu nối citation -> viewer (đến từ ChatMessage, xem citationHref()): ?page=&highlight=
  // Chỉ đọc 1 lần lúc mount trang — không cần đồng bộ lại nếu params đổi sau đó.
  const highlightPageParam = searchParams.get("page");
  const highlightPage = highlightPageParam ? Number(highlightPageParam) : null;
  const highlightSnippet = searchParams.get("highlight") || null;

  const [doc, setDoc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState("");
  const [posting, setPosting] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [ingesting, setIngesting] = useState(false);
  // Chỉ phản ánh trạng thái trong phiên hiện tại (không có field ingested ở backend) -
  // reload trang sẽ mất, không phải bug.
  const [ingested, setIngested] = useState(false);

  const [addToCollectionModalOpen, setAddToCollectionModalOpen] = useState(false);
  const [myCollections, setMyCollections] = useState([]);
  const [loadingCollections, setLoadingCollections] = useState(false);
  const [selectedCollectionIds, setSelectedCollectionIds] = useState([]);
  const [addingToCollections, setAddingToCollections] = useState(false);
  const [creatingCollection, setCreatingCollection] = useState(false);

  const [activeTab, setActiveTab] = useState("comments");
  const [relatedDocs, setRelatedDocs] = useState([]);
  const [loadingRelated, setLoadingRelated] = useState(false);
  const [relatedLoaded, setRelatedLoaded] = useState(false);

  // Modal sửa metadata
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [subjects, setSubjects] = useState([]);

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

  const handleUpdateComment = async (commentId, content) => {
    if (!content.trim()) return;
    try {
      await documentApi.updateComment(commentId, content.trim());
      showSuccess("Đã cập nhật bình luận.");
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  const handleDeleteComment = async (commentId) => {
    try {
      await documentApi.deleteComment(commentId);
      showSuccess("Đã xoá bình luận.");
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  // Sửa metadata (title/description/subjects). data: { title?, description?, subjectIds? }
  const openEditModal = async () => {
    setEditModalOpen(true);
    if (subjects.length === 0) {
      try {
        const list = await subjectApi.getAll();
        setSubjects(list);
      } catch (err) {
        showError(err.message);
      }
    }
  };

  const closeEditModal = () => setEditModalOpen(false);

  const handleUpdateDocument = async (data) => {
    setEditSubmitting(true);
    try {
      await documentApi.updateDocument(id, data);
      showSuccess("Đã cập nhật tài liệu.");
      setEditModalOpen(false);
      await load();
    } catch (err) {
      showError(err.message);
    } finally {
      setEditSubmitting(false);
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
      await load();
    } catch (err) {
      showError(err.message || "Không thể chuẩn bị tài liệu cho AI, vui lòng thử lại.");
    } finally {
      setIngesting(false);
    }
  };

  const handleToggleVisibility = async () => {
    try {
      // BE trả về document sau khi đổi kèm kết quả kiểm duyệt AI.
      const updated = await documentApi.toggleVisibility(id);
      if (updated && typeof updated === "object" && "visibility" in updated) {
        if (updated.visibility === "PUBLIC") {
          showSuccess("Đã công khai tài liệu. AI kiểm duyệt: đạt.");
        } else if (updated.moderationStatus === "REJECTED") {
          showError(
            "AI chưa cho công khai: " +
              (updated.moderationReason || "nội dung cần Admin xem xét.") +
              " Bạn có thể gửi kháng cáo."
          );
        } else {
          showSuccess("Đã chuyển tài liệu về riêng tư.");
        }
      }
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

  // Lazy: chỉ gọi API khi tab "Liên quan" thực sự được mở, không gọi kèm mỗi lần load doc.
  const selectTab = (tab) => {
    setActiveTab(tab);
    if (tab === "related" && !relatedLoaded) {
      setLoadingRelated(true);
      aiApi
        .getRelatedDocuments(id)
        .then((data) => {
          setRelatedDocs(data);
          setRelatedLoaded(true);
        })
        .catch((err) => showError(err.message))
        .finally(() => setLoadingRelated(false));
    }
  };

  const goToDocument = (documentId) => {
    navigate(buildRoute(ROUTES.DOCUMENT_DETAIL, { id: documentId }));
  };

  const openAddToCollectionModal = async () => {
    setAddToCollectionModalOpen(true);
    setSelectedCollectionIds([]);
    setLoadingCollections(true);
    try {
      const list = await collectionApi.listMyCollections();
      setMyCollections(list);
    } catch (err) {
      showError(err.message);
    } finally {
      setLoadingCollections(false);
    }
  };

  const closeAddToCollectionModal = () => setAddToCollectionModalOpen(false);

  const toggleSelectCollection = (collectionId) => {
    setSelectedCollectionIds((prev) =>
      prev.includes(collectionId) ? prev.filter((x) => x !== collectionId) : [...prev, collectionId]
    );
  };

  // Thêm là THAM CHIẾU (DEC-007) — không copy/move, tài liệu vẫn còn nguyên ở đây.
  const handleAddToCollections = async () => {
    if (selectedCollectionIds.length === 0) return;
    setAddingToCollections(true);
    try {
      await Promise.all(
        selectedCollectionIds.map((cid) => collectionApi.addDocuments(cid, [Number(id)]))
      );
      showSuccess("Đã thêm vào collection.");
      setAddToCollectionModalOpen(false);
    } catch (err) {
      showError(err.message);
    } finally {
      setAddingToCollections(false);
    }
  };

  const handleCreateCollectionAndAdd = async (name) => {
    setCreatingCollection(true);
    try {
      const created = await collectionApi.createCollection(name);
      await collectionApi.addDocuments(created.id, [Number(id)]);
      setMyCollections((prev) => [created, ...prev]);
      showSuccess(`Đã tạo "${created.name}" và thêm tài liệu vào đó.`);
      setAddToCollectionModalOpen(false);
    } catch (err) {
      showError(err.message);
    } finally {
      setCreatingCollection(false);
    }
  };

  return {
    doc,
    loading,
    isLikelyOwner,
    currentUserName: user?.fullName ?? null,
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
    documentsRoute: buildRoute(ROUTES.DOCUMENTS),

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
  };
}
