import { useEffect, useMemo, useState } from "react";
import * as adminApi from "../../../../api/adminApi";
import { useToast } from "../../../../hooks/useToast";

const PAGE_SIZE = 8;

export function useAdminUsersPage() {
  const { showSuccess, showError } = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearchValue] = useState("");
  const [page, setPage] = useState(0);

  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [updating, setUpdating] = useState(false);
  const [statusUpdatingId, setStatusUpdatingId] = useState(null);

  useEffect(() => {
    const loadUsers = async () => {
      setLoading(true);
      try {
        setUsers(await adminApi.getAllUsers());
      } catch (err) {
        showError(err.message);
      } finally {
        setLoading(false);
      }
    };
    loadUsers();
    // useToast returns lightweight pub/sub callbacks; load once when the page mounts.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filteredUsers = useMemo(() => {
    const query = search.trim().toLocaleLowerCase("vi-VN");
    if (!query) return users;
    return users.filter((user) =>
      [user.fullName, user.email].some((value) =>
        value?.toLocaleLowerCase("vi-VN").includes(query)
      )
    );
  }, [search, users]);

  const totalPages = Math.ceil(filteredUsers.length / PAGE_SIZE);
  const visibleUsers = filteredUsers.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  useEffect(() => {
    if (totalPages > 0 && page >= totalPages) setPage(totalPages - 1);
  }, [page, totalPages]);

  const setSearch = (value) => {
    setSearchValue(value);
    setPage(0);
  };

  const createUser = async (payload) => {
    setCreating(true);
    try {
      const created = await adminApi.createUser(payload);
      setUsers((current) => [...current, created]);
      setCreateModalOpen(false);
      showSuccess("Đã tạo tài khoản.");
      return true;
    } catch (err) {
      showError(err.message);
      return false;
    } finally {
      setCreating(false);
    }
  };

  // Chỉ đổi role — nhưng vẫn gửi kèm fullName/avatarUrl HIỆN TẠI của user (từ editTarget,
  // không đổi) vì không chắc BE có bỏ qua field null hay ghi đè thành null khi thiếu field.
  const updateUserRole = async (role) => {
    if (!editTarget) return false;
    setUpdating(true);
    try {
      const payload = {
        fullName: editTarget.fullName,
        avatarUrl: editTarget.avatarUrl ?? null,
        role,
      };
      const updated = await adminApi.updateUser(editTarget.id, payload);
      setUsers((current) => current.map((user) => (user.id === updated.id ? updated : user)));
      setEditTarget(null);
      showSuccess("Đã đổi vai trò.");
      return true;
    } catch (err) {
      showError(err.message);
      return false;
    } finally {
      setUpdating(false);
    }
  };

  const toggleUserStatus = async (user) => {
    const nextStatus = user.status === "BANNED" ? "ACTIVE" : "BANNED";
    setStatusUpdatingId(user.id);
    try {
      const updated = await adminApi.updateUserStatus(user.id, nextStatus);
      setUsers((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      showSuccess(nextStatus === "BANNED" ? "Đã khóa tài khoản." : "Đã mở khóa tài khoản.");
    } catch (err) {
      showError(err.message);
    } finally {
      setStatusUpdatingId(null);
    }
  };

  return {
    loading,
    search,
    setSearch,
    page,
    setPage,
    totalPages,
    visibleUsers,
    filteredUserCount: filteredUsers.length,
    createModalOpen,
    creating,
    openCreateModal: () => setCreateModalOpen(true),
    closeCreateModal: () => setCreateModalOpen(false),
    createUser,
    editTarget,
    updating,
    openEditModal: setEditTarget,
    closeEditModal: () => setEditTarget(null),
    updateUserRole,
    statusUpdatingId,
    toggleUserStatus,
  };
}
