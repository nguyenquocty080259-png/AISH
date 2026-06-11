import axios from 'axios';
import { getAccessToken } from '../services/authService';

const BASE_URL = 'http://localhost:8080/api/documents';

const api = axios.create({
    baseURL: BASE_URL,
    withCredentials: true
});

// Tự động gắn token vào mọi request
api.interceptors.request.use((config) => {
    const token = getAccessToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const documentApi = {
    getAll: () => api.get(''),
    upload: (formData) => api.post('/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
    update: (id, data) => api.put(`/${id}`, data),
    toggleVisibility: (id) => api.put(`/${id}/toggle-visibility`),
    delete: (id) => api.delete(`/${id}`),
    download: (id) => api.get(`/${id}/download`, { responseType: 'blob' }),
    getTrash: () => api.get('/trash'),
    restore: (id) => api.put(`/${id}/restore`),
    toggleFavorite: (id) => api.post(`/${id}/favorite`),
    addComment: (id, content) => api.post(`/${id}/comment`, content, {
        headers: { 'Content-Type': 'text/plain' }
    }),
    rate: (id, star) => api.post(`/${id}/rate?star=${star}`),
    getById: (id) => api.get(`/${id}`),
};