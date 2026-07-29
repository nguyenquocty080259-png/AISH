import axios from "axios";
import i18n from "../i18n";
import { STORAGE_KEYS } from "../constants/storageKeys";

const apiClient = axios.create({
    baseURL: "http://localhost:8080/api",
});


apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // Báo cho backend biết ngôn ngữ đang chọn để trả message lỗi (ngoài auth) theo VI/EN.
  const language = i18n.language?.startsWith("en") ? "en" : "vi";
  config.headers["Accept-Language"] = language;
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
      backendMessage || error.message || i18n.t("common.genericError");

    return Promise.reject({
      status,
      message: normalizedMessage,
      raw: error,
    });
  }
);

export default apiClient;
