import axios from "axios";
import { STORAGE_KEYS } from "../constants/storageKeys";

// const apiClient = axios.create({
//   baseURL: import.meta.env.VITE_API_URL,
// });

const apiClient = axios.create({
    baseURL: "http://localhost:8080/api",
});

console.log("VITE_API_URL =", import.meta.env.VITE_API_URL);
console.log("apiClient baseURL =", apiClient.defaults.baseURL);

apiClient.interceptors.request.use((config) => {
  console.log("=== REQUEST ===");
  console.log("Base URL:", config.baseURL);
  console.log("URL:", config.url);
  console.log("Full:", (config.baseURL || "") + config.url);
  return config;
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const backendMessage = error.response?.data?.message;

    if (status === 401) {
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);
      window.dispatchEvent(new Event("auth:unauthorized"));
    }

    const normalizedMessage =
      backendMessage || error.message || "Đã có lỗi xảy ra, vui lòng thử lại.";

    return Promise.reject({
      status,
      message: normalizedMessage,
      raw: error,
    });
  }
);

export default apiClient;