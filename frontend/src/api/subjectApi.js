import axios from 'axios';
import { getAccessToken } from '../services/authService';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/subjects',
});

api.interceptors.request.use((config) => {
    const token = getAccessToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

export const subjectApi = {
    getAll: () => api.get(''),
    create: (name) => api.post('', { name }),
};