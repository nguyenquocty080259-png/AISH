import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as documentApi from "../../../api/documentApi";
import * as aiApi from "../../../api/aiApi";
import * as collectionApi from "../../../api/collectionApi";
import * as subjectApi from "../../../api/subjectApi";
import * as shareApi from "../../../api/shareApi";
import * as adminApi from "../../../api/adminApi";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES, buildRoute } from "../../../constants/routes";
import { ROLES } from "../../../constants/roles";

// Bỏ dấu tiếng Việt để so khớp không phân biệt hoa/thường và có dấu/không dấu.
const COMBINING_MARKS_RE = /[̀-ͯ]/g;
function normalizeForSearch(text) {
  return (text ?? "")
    .normalize("NFD")
    .replace(COMBINING_MARKS_RE, "")
    .toLowerCase();
}

// Nguồn duy nhất để quyết định trình xem nào hiển thị (A3/T7) — trước đây là 1 IIFE lồng
// nhau trong JSX của DocumentDetailPage, giờ tách ra để thêm định dạng mới không phải sửa
// ternary lồng nhau. Cùng tiêu chí với backend (DocEmbeddingServiceImpl.resolveIngestFormat)
// cho các nhánh dùng chung MIME, nhưng đây là whitelist xuôi (chỉ định dạng có viewer riêng
// mới thoát khỏi "other") vì FE cần biết chính xác nên render component nào.
export function resolveViewerKind(fileType, fileName) {
  const type = (fileType || "").toLowerCase();
  const name = (fileName || "").toLowerCase();

  if (type.includes("image") || /\.(png|jpe?g|gif|webp|bmp|svg)$/.test(name)) return "image";
  if (type.includes("pdf") || name.endsWith(".pdf")) return "pdf";
  if (type === "text/plain" || name.endsWith(".txt")) return "txt";
  if (type.includes("wordprocessingml") || name.endsWith(".docx")) return "docx";
  if (type.includes("spreadsheetml") || name.endsWith(".xlsx")) return "xlsx";
  if (type.includes("presentationml") || name.endsWith(".pptx")) return "pptx";
  return "other";
}

// Kind cần 1 blob thô từ /preview để hiển thị trực tiếp (ảnh <img>, PDF qua react-pdf, hoặc
// link "Mở file trong tab mới" cho định dạng không có viewer riêng). DOCX/XLSX/PPTX/TXT tự
// fetch blob/text riêng bên trong component viewer của chúng (xem DocxViewer, XlsxViewer,
// TextFileViewer, ExtractedTextViewer) nên không cần state chung ở đây.
const RAW_BLOB_KINDS = new Set(["image", "pdf", "other"]);

/**
 * Hook TRUNG TÂM cho trang chi tiết tài liệu — gom toàn bộ state và hành động: nạp tài liệu, xem
 * trước (chọn đúng viewer theo định dạng file), bình luận/đánh giá/yêu thích, sửa/xoá/tải về,
 * bật/tắt công khai (qua kiểm duyệt AI), ingest cho AI chat, chia sẻ, thêm vào bộ sưu tập, xem
 * tài liệu liên quan, và duyệt nhanh của Admin ngay trên trang.
 */
export function useDocumentDetailPage() {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, role } = useAuth();
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
  const [blockedComment, setBlockedComment] = useState(null);
  const [downloading, setDownloading] = useState(false);
  const [ingesting, setIngesting] = useState(false);
  // Chỉ phản ánh trạng thái trong phiên hiện tại (không có field ingested ở backend) -
  // reload trang sẽ mất, không phải bug.
  const [ingested, setIngested] = useState(false);

  // Blob thô cho ảnh/PDF/định dạng không có viewer riêng (xem RAW_BLOB_KINDS) — object URL,
  // phải revoke khi đổi tài liệu hoặc unmount để không rò rỉ bộ nhớ.
  const [previewBlobUrl, setPreviewBlobUrl] = useState(null);
  const [previewBlobError, setPreviewBlobError] = useState(false);

  const [addToCollectionModalOpen, setAddToCollectionModalOpen] = useState(false);
  const [myCollections, setMyCollections] = useState([]);
  const [loadingCollections, setLoadingCollections] = useState(false);
  const [selectedCollectionIds, setSelectedCollectionIds] = useState([]);
  const [addingToCollections, setAddingToCollections] = useState(false);
  const [creatingCollection, setCreatingCollection] = useState(false);
  const [collectionQuery, setCollectionQuery] = useState("");

  const [activeTab, setActiveTab] = useState("comments");
  const [relatedDocs, setRelatedDocs] = useState([]);
  const [loadingRelated, setLoadingRelated] = useState(false);
  const [relatedLoaded, setRelatedLoaded] = useState(false);

  // Modal sửa metadata
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [subjects, setSubjects] = useState([]);

  // Modal chia sẻ
  const [shareModalOpen, setShareModalOpen] = useState(false);
  const [sharing, setSharing] = useState(false);
  // Danh sách người đang được chia sẻ (chỉ chủ sở hữu) — hiển thị trong modal để gỡ từng người.
  const [shareRecipients, setShareRecipients] = useState([]);
  const [loadingRecipients, setLoadingRecipients] = useState(false);

  // Gọi API GET /documents/{id} — nạp (hoặc nạp lại) chi tiết tài liệu.
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

  // Chỉ fetch blob thô khi viewer thực sự cần (RAW_BLOB_KINDS) — DOCX/XLSX/PPTX/TXT tự lo lấy
  // dữ liệu bên trong viewer riêng. Phụ thuộc vào fileType/fileName (giá trị nguyên thuỷ) thay
  // vì cả object doc — tránh fetch lại mỗi khi load() chạy lại do thao tác khác (thích, đánh
  // giá, bình luận...) mà file không đổi.
  useEffect(() => {
    if (!doc?.id || !RAW_BLOB_KINDS.has(resolveViewerKind(doc.fileType, doc.fileName))) {
      setPreviewBlobUrl(null);
      setPreviewBlobError(false);
      return;
    }
    let cancelled = false;
    let objectUrl = null;
    setPreviewBlobUrl(null);
    setPreviewBlobError(false);
    documentApi
      .previewFile(doc.id)
      .then((blob) => {
        if (cancelled) return;
        objectUrl = window.URL.createObjectURL(blob);
        setPreviewBlobUrl(objectUrl);
      })
      .catch(() => {
        if (!cancelled) setPreviewBlobError(true);
      });
    return () => {
      cancelled = true;
      if (objectUrl) window.URL.revokeObjectURL(objectUrl);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [doc?.id, doc?.fileType, doc?.fileName]);

  // GHI CHÚ: DocumentResponseDTO chỉ trả ownerName (string), không có ownerId,
  // nên đây chỉ là check tương đối ở FE để ẨN/HIỆN nút quản lý cho gọn UI.
  // Quyền thực sự vẫn do backend kiểm tra (toggle-visibility/delete) theo user đang đăng nhập.
  const isLikelyOwner = Boolean(
    user?.fullName && doc?.ownerName && user.fullName === doc.ownerName
  );

  // Bật/tắt yêu thích rồi nạp lại tài liệu.
  const handleToggleFavorite = async () => {
    try {
      await documentApi.toggleFavorite(id);
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  // Chấm điểm 1-5 sao rồi nạp lại tài liệu.
  const handleRate = async (star) => {
    try {
      await documentApi.rateDocument(id, star);
      showSuccess(t("docDetail.toasts.rated"));
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  // Gửi bình luận mới. Bị chặn vì trúng từ khoá cấm (lỗi 422 kèm blocked=true) thì mở hộp thoại
  // khiếu nại (blockedComment) thay vì báo lỗi thẳng — cho người dùng cơ hội giải thích.
  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;
    setPosting(true);
    try {
      await documentApi.addComment(id, commentText.trim());
      setCommentText("");
      await load();
    } catch (err) {
      if (err.status === 422 && err.raw?.response?.data?.blocked) {
        setBlockedComment({
          mode: "create",
          content: commentText.trim(),
          reason: err.raw.response.data.reason,
        });
      } else {
        showError(err.message);
      }
    } finally {
      setPosting(false);
    }
  };

  // Sửa bình luận — cùng luồng chặn/khiếu nại như handleAddComment.
  const handleUpdateComment = async (commentId, content) => {
    if (!content.trim()) return false;
    try {
      await documentApi.updateComment(commentId, content.trim());
      showSuccess(t("docDetail.toasts.commentUpdated"));
      await load();
      return true;
    } catch (err) {
      if (err.status === 422 && err.raw?.response?.data?.blocked) {
        setBlockedComment({
          mode: "update",
          commentId,
          content: content.trim(),
          reason: err.raw.response.data.reason,
        });
      } else {
        showError(err.message);
      }
      return false;
    }
  };

  const dismissBlockedComment = () => setBlockedComment(null);

  // Gửi lại bình luận bị chặn kèm lý do khiếu nại — vào thẳng trạng thái chờ Admin duyệt.
  const disputeBlockedComment = async (disputeNote) => {
    if (!blockedComment) return false;
    setPosting(true);
    try {
      const options = { dispute: true, disputeNote: disputeNote.trim() };
      if (blockedComment.mode === "create") {
        await documentApi.addComment(id, blockedComment.content, options);
        setCommentText("");
      } else {
        await documentApi.updateComment(
          blockedComment.commentId, blockedComment.content, options);
      }
      setBlockedComment(null);
      showSuccess(t("docDetail.toasts.commentPendingReview"));
      await load();
      return true;
    } catch (err) {
      showError(err.message);
      return false;
    } finally {
      setPosting(false);
    }
  };

  // Xoá bình luận (chỉ tác giả).
  const handleDeleteComment = async (commentId) => {
    try {
      await documentApi.deleteComment(commentId);
      showSuccess(t("docDetail.toasts.commentDeleted"));
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

  // Lưu thay đổi tiêu đề/mô tả/môn học từ modal sửa.
  const handleUpdateDocument = async (data) => {
    setEditSubmitting(true);
    try {
      await documentApi.updateDocument(id, data);
      showSuccess(t("docDetail.toasts.docUpdated"));
      setEditModalOpen(false);
      await load();
    } catch (err) {
      showError(err.message);
    } finally {
      setEditSubmitting(false);
    }
  };

  // Tải file gốc về máy: gọi API lấy blob, đọc tên file thật từ header Content-Disposition,
  // rồi tạo link ẩn để trình duyệt tự tải xuống.
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
      showError(err.message || t("docDetail.toasts.downloadError"));
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
        showSuccess(res.message || t("docDetail.toasts.ingestSuccess"));
      } else {
        showError(res.message || t("docDetail.toasts.ingestFail"));
      }
      await load();
    } catch (err) {
      showError(err.message || t("docDetail.toasts.ingestError"));
    } finally {
      setIngesting(false);
    }
  };

  // Bấm "Công khai"/"Riêng tư": gọi API xin đổi visibility rồi hiện thông báo phù hợp với kết
  // quả kiểm duyệt AI trả về (chờ duyệt / công khai ngay / bị từ chối kèm lý do).
  const handleToggleVisibility = async () => {
    try {
      // BE trả về document sau khi đổi kèm kết quả kiểm duyệt AI. Yêu cầu công khai KHÔNG còn
      // tự động public: nó dừng ở ADMIN_PENDING cho tới khi Admin duyệt (xem toggleVisibility
      // ở backend), nên nhánh chờ duyệt phải được xét TRƯỚC.
      const updated = await documentApi.toggleVisibility(id);
      if (updated && typeof updated === "object" && "visibility" in updated) {
        if (updated.moderationStatus === "ADMIN_PENDING") {
          if (updated.aiScreenOutcome === "FLAG") {
            showSuccess(t("docDetail.toasts.publicPendingFlagged"));
          } else {
            showSuccess(t("docDetail.toasts.publicPending"));
          }
        } else if (updated.visibility === "PUBLIC") {
          showSuccess(t("docDetail.toasts.madePublic"));
        } else if (updated.moderationStatus === "REJECTED") {
          showError(
            t("docDetail.toasts.publicRejected", {
              reason: updated.moderationReason || t("docDetail.toasts.publicRejectedReason"),
            })
          );
        } else {
          showSuccess(t("docDetail.toasts.madePrivate"));
        }
      }
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  // --- Xem xét của Admin ngay trên trang chi tiết ---
  // Admin đọc được cả tài liệu PRIVATE đang chờ duyệt (backend miễn trừ theo role), nên chỗ
  // quyết định hợp lý nhất là ngay sau khi xem nội dung. Quyền thật vẫn do /api/admin/** gác.
  const isAdmin = role === ROLES.ADMIN;
  const [adminReviewing, setAdminReviewing] = useState(null); // "approve" | "reject" | null

  // Admin duyệt/từ chối tài liệu ngay trên trang chi tiết (không cần qua trang quản trị riêng).
  const handleAdminReview = async (action) => {
    setAdminReviewing(action);
    try {
      if (action === "approve") {
        await adminApi.approveDocumentReview(id);
        showSuccess(t("docDetail.adminReview.approved"));
      } else {
        await adminApi.removeDocumentReview(id);
        showSuccess(t("docDetail.adminReview.rejected"));
      }
      await load();
    } catch (err) {
      showError(err.message);
    } finally {
      setAdminReviewing(null);
    }
  };
  // Xoá mềm tài liệu (vào thùng rác) rồi điều hướng về danh sách tài liệu.
  const handleDelete = async () => {
    try {
      await documentApi.deleteDocument(id);
      showSuccess(t("docDetail.toasts.docDeleted"));
      navigate(ROUTES.DOCUMENTS);
    } catch (err) {
      showError(err.message);
    }
  };

  // Điều hướng sang trang AI Chat, kèm sẵn documentId để tự bật hỏi-đáp về tài liệu này.
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

  // Mở modal "Thêm vào bộ sưu tập" — nạp danh sách bộ sưu tập của tôi.
  const openAddToCollectionModal = async () => {
    setAddToCollectionModalOpen(true);
    setSelectedCollectionIds([]);
    setCollectionQuery("");
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

  const normalizedCollectionQuery = normalizeForSearch(collectionQuery);
  const filteredCollections = normalizedCollectionQuery
    ? myCollections.filter((c) => normalizeForSearch(c.name).includes(normalizedCollectionQuery))
    : myCollections;

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
      showSuccess(t("docDetail.toasts.addedToCollection"));
      setAddToCollectionModalOpen(false);
    } catch (err) {
      showError(err.message);
    } finally {
      setAddingToCollections(false);
    }
  };

  // Tạo bộ sưu tập mới NGAY TRONG modal rồi thêm luôn tài liệu này vào đó.
  const handleCreateCollectionAndAdd = async (name) => {
    setCreatingCollection(true);
    try {
      const created = await collectionApi.createCollection(name);
      await collectionApi.addDocuments(created.id, [Number(id)]);
      setMyCollections((prev) => [created, ...prev]);
      showSuccess(t("docDetail.toasts.createdAndAdded", { name: created.name }));
      setAddToCollectionModalOpen(false);
    } catch (err) {
      showError(err.message);
    } finally {
      setCreatingCollection(false);
    }
  };

  // Nạp danh sách người đang được chia sẻ tài liệu (chỉ chủ sở hữu gọi được).
  const loadShareRecipients = async () => {
    setLoadingRecipients(true);
    try {
      const data = await shareApi.getShareRecipients(id);
      setShareRecipients(Array.isArray(data) ? data : []);
    } catch (err) {
      showError(err.message);
      setShareRecipients([]);
    } finally {
      setLoadingRecipients(false);
    }
  };

  const openShareModal = () => {
    setShareModalOpen(true);
    loadShareRecipients();
  };
  const closeShareModal = () => setShareModalOpen(false);

  // payload: { mode, email?, permission? }. Trả về { shareMode, shareToken } hoặc null nếu lỗi
  // (để ShareModal hiển thị link khi mode = ANYONE_WITH_LINK). Sau khi chia sẻ, reload để cập
  // nhật visibility (SHARED) trên UI và làm mới danh sách người được chia sẻ.
  const handleShare = async (payload) => {
    setSharing(true);
    try {
      const res = await shareApi.shareDocument(id, payload);
      if (payload.mode === "RESTRICTED") {
        showSuccess(t("docDetail.toasts.shared"));
        await loadShareRecipients();
      } else if (payload.mode === "ANYONE_WITH_LINK") {
        showSuccess(t("docDetail.toasts.linkShareOn"));
      } else {
        showSuccess(t("docDetail.toasts.linkShareOff"));
      }
      await load();
      return res;
    } catch (err) {
      showError(err.message);
      return null;
    } finally {
      setSharing(false);
    }
  };

  // Gỡ chia sẻ 1 người (hard delete row). Sau khi gỡ, làm mới danh sách trong modal.
  const handleRevokeShare = async (userId) => {
    try {
      await shareApi.revokeShare(id, userId);
      showSuccess(t("docDetail.toasts.shareRevoked"));
      await loadShareRecipients();
    } catch (err) {
      showError(err.message);
    }
  };

  return {
    doc,
    loading,
    isLikelyOwner,
    currentUserName: user?.fullName ?? null,
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
    documentDetailPath: buildRoute(ROUTES.DOCUMENT_DETAIL, { id }),
    handleDownload,
    handleIngest,
    handleToggleVisibility,
    handleDelete,
    isAdmin,
    adminReviewing,
    handleAdminReview,
    goAskAi,
    documentsRoute: buildRoute(ROUTES.DOCUMENTS),

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
  };
}
