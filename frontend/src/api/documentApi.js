import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/documents';

export const documentApi = {
    // 1. Lấy toàn bộ danh sách
    getAll: () => axios.get(BASE_URL),
    
    // 2. Lấy chi tiết theo ID
    getById: (id) => axios.get(`${BASE_URL}/${id}`),
    
    // 3. Upload file
    upload: (formData) => axios.post(`${BASE_URL}/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
    
    // 4. Xóa tài liệu
    delete: (id) => axios.delete(`${BASE_URL}/${id}`),
    
    // 5. Tải xuống file (Download)
    download: (id) => axios.get(`${BASE_URL}/${id}/download`, {
        responseType: 'blob', 
    }),
};