import { useEffect, useState } from "react";

// Toast dùng pub/sub đơn giản thay vì tạo ToastContext riêng,
// vì kiến trúc chỉ cho phép context/ chứa AuthContext.
// Hook không render UI (đúng rule), phần UI hiển thị toast nằm trong App.jsx.
const listeners = new Set();
let idCounter = 0;

function emit(message, type) {
  const toast = { id: ++idCounter, message, type };
  listeners.forEach((listener) => listener(toast));
}

// Gọi từ bất kỳ hook/page nào cần báo lỗi/thành công cho người dùng.
export function useToast() {
  return {
    showToast: (message, type = "info") => emit(message, type),
    showSuccess: (message) => emit(message, "success"),
    showError: (message) => emit(message, "error"),
  };
}

// CHỈ dùng trong App.jsx để lấy danh sách toast hiện tại và render ra UI.
export function useToastListener() {
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    const handleNewToast = (toast) => {
      setToasts((prev) => [...prev, toast]);
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== toast.id));
      }, 3500);
    };
    listeners.add(handleNewToast);
    return () => listeners.delete(handleNewToast);
  }, []);

  const dismiss = (id) => setToasts((prev) => prev.filter((t) => t.id !== id));

  return { toasts, dismiss };
}
