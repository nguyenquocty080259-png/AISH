import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/documents';

const api = axios.create({
    baseURL: BASE_URL,
    withCredentials: true 
});

export const documentApi = {
    // 1. Lấy danh sách tài liệu hiện có (Chưa bị xóa mềm)
    getAll: () => api.get(''),
    
    // 2. Upload tài liệu kèm file vật lý thực tế
    upload: (formData) => api.post('/upload', formData, { 
        headers: { 'Content-Type': 'multipart/form-data' } 
    }),
    
    // 3. Cập nhật thông tin nhanh (Sửa tiêu đề/mô tả)
    update: (id, data) => api.put(`/${id}`, data),
    
    // 4. Chuyển đổi trạng thái riêng tư (PUBLIC / PRIVATE)
    toggleVisibility: (id) => api.put(`/${id}/toggle-visibility`),
    
    // 5. Xóa mềm tài liệu (Đưa vào thùng rác)
    delete: (id) => api.delete(`/${id}`),
    
    // 6. Tải xuống file (Download)
    download: (id) => api.get(`/${id}/download`, { responseType: 'blob' }),
    
    // 7. Lấy danh sách tài liệu trong Thùng rác
    getTrash: () => api.get('/trash'),
    
    // 8. Khôi phục tài liệu từ Thùng rác về trang chủ
    restore: (id) => api.put(`/${id}/restore`),

    // =========================================================================
    // BỔ SUNG CÁC HÀM TƯƠNG TÁC THỰC TẾ THEO ĐÚNG SƠ ĐỒ BẢNG (ĐÃ THIẾU)
    // =========================================================================
    
    // 9. Thả tim / Yêu thích tài liệu (Khớp cổng @PostMapping("/{id}/favorite") ở BE)
    toggleFavorite: (id) => api.post(`/${id}/favorite`),
    
    // 10. Đăng bình luận thảo luận (Khớp cổng @PostMapping("/{id}/comment") ở BE)
    addComment: (id, content) => api.post(`/${id}/comment`, content, { 
        headers: { 'Content-Type': 'text/plain' } 
    }),
    
    // 11. Đánh giá số sao (Khớp cổng @PostMapping("/{id}/rate") ở BE)
    rate: (id, star) => api.post(`/${id}/rate?star=${star}`),
};