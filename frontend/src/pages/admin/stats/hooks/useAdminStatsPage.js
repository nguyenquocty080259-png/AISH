import { useEffect, useState } from "react";
import i18n from "../../../../i18n";
import * as adminApi from "../../../../api/adminApi";

// Hook logic trang Admin THỐNG KÊ: các ô số liệu tổng quan, bấm vào ô "Người dùng"/"Tài liệu"
// thì mở bảng chi tiết tương ứng ngay bên dưới (chỉ 1 trong 2 bảng hiện tại một lúc).
export function useAdminStatsPage() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [users, setUsers] = useState([]);
    const [showUsers, setShowUsers] = useState(false);
    const [documents, setDocuments] = useState([]);
    const [showDocuments, setShowDocuments] = useState(false);
    const [documentTitle, setDocumentTitle] = useState("");
    const [documentPage, setDocumentPage] = useState(0);
    const [documentTotalPages, setDocumentTotalPages] = useState(1);
    const [documentTotalElements, setDocumentTotalElements] = useState(0);
    const [documentVisibility, setDocumentVisibility] = useState(null);

    // Mở bảng tài liệu theo bộ lọc visibility (null = tất cả), đóng bảng người dùng nếu đang mở.
    const loadDocuments = (visibility = null,
        title = i18n.t("admin.stats.allDocsTitle")
    ) => {
        setDocumentPage(0);
        setDocumentVisibility(visibility);
        setDocumentTitle(title);
        setShowUsers(false);
        setShowDocuments(true);
    };

    useEffect(() => {
        if (!showDocuments) return undefined;

        let active = true;
        const fetchDocuments = async () => {
            try {
                const page = await adminApi.listDocuments(documentPage, 10, documentVisibility);
                if (!active) return;
                setDocuments(page.content ?? []);
                setDocumentTotalPages(Math.max(page.totalPages ?? 0, 1));
                setDocumentTotalElements(page.totalElements ?? 0);
                setDocumentPage(page.number ?? documentPage);
            } catch (error) {
                if (active) console.error("Lỗi tải tài liệu:", error);
            }
        };

        fetchDocuments();
        return () => {
            active = false;
        };
    }, [showDocuments, documentPage, documentVisibility]);
    // Gọi API GET /admin/stats — nạp số liệu tổng quan.
    const loadStats = async () => {
        try {
            const data = await adminApi.getStats();
            setStats(data);
        } catch (error) {
            console.error("Lỗi tải thống kê:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadStats();
    }, []);

    // Gọi API GET /admin/users — nạp toàn bộ người dùng (chỉ gọi 1 lần, cache lại trong state).
    const loadUsers = async () => {
        try {
            const data = await adminApi.getAllUsers();
            setUsers(data);
        } catch (error) {
            console.error("Lỗi tải danh sách người dùng:", error);
        }
    };

    // Bấm ô "Tổng người dùng": nạp danh sách (nếu chưa có), đóng bảng tài liệu, bật/tắt bảng người dùng.
    const toggleUsers = async () => {

    if (!showUsers && users.length === 0) {
        await loadUsers();
    }

    // Đóng bảng tài liệu
    setShowDocuments(false);

    // Mở/đóng bảng user
    setShowUsers((prev) => !prev);
    };

    return {
        stats, 
        loading, 
        users, 
        showUsers, 
        toggleUsers,
        documents,
        showDocuments,
        documentTitle, 
        loadDocuments,
        documentPage,
        setDocumentPage,
        documentTotalPages,
        documentTotalElements};
}
