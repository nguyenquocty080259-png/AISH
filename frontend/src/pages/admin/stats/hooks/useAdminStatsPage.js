import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";

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

    const loadDocuments = (visibility = null,
        title = "Danh sách tài liệu"
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
    // Load thống kê
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

    // Load danh sách user
    const loadUsers = async () => {
        try {
            const data = await adminApi.getAllUsers();
            setUsers(data);
        } catch (error) {
            console.error("Lỗi tải danh sách người dùng:", error);
        }
    };

    // Click card Tổng người dùng
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
