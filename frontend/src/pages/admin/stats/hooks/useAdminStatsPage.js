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
    //load tổng tài liệu
    const toggleDocuments = async () => {

        if (!showDocuments && documents.length === 0) {

            const page = await adminApi.listDocuments();

            setDocuments(page.content);
        }

        setShowDocuments(prev => !prev);
    };
    const loadDocuments = async (visibility = null, 
        title = "Danh sách tài liệu"
    ) => {
    try {

        const page = await adminApi.listDocuments(
        0,
        20,
        visibility
        );

        setDocuments(page.content ?? []);

        setDocumentTitle(title);

        // Đóng bảng user
        setShowUsers(false);

        // Luôn mở bảng tài liệu
        setShowDocuments(true);

    } catch (error) {
        console.error("Lỗi tải tài liệu:", error);
    }
    };
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
        loadDocuments};
}