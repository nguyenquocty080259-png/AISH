import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";

export function useAdminStatsPage() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [users, setUsers] = useState([]);
    const [showUsers, setShowUsers] = useState(false);

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
        // Nếu chưa mở và chưa load thì gọi API
        if (!showUsers && users.length === 0) {
            await loadUsers();
        }

        setShowUsers(prev => !prev);
    };

    return {stats, loading, users, showUsers, toggleUsers};
}