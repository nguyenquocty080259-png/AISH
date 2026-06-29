import axios from "axios";
import { STORAGE_KEYS } from "../constants/storageKeys";

// Đây là Axios instance DUY NHẤT của toàn hệ thống.
// Mọi file trong api/ phải import apiClient từ đây, không import axios trực tiếp.
// Không set sẵn Content-Type ở đây: axios tự nhận diện JSON (object thường)
// và multipart (FormData, dùng cho upload file) để set Content-Type + boundary
// chính xác cho từng request.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

// Gắn access token vào mọi request (nếu có)
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Chuẩn hoá lỗi trả về từ backend.
// Backend hiện CHƯA có GlobalExceptionHandler, nên RuntimeException sẽ rơi về
// body lỗi mặc định của Spring Boot dạng { timestamp, status, error, message, path }.
// apiClient luôn cố gắng trả ra message dễ đọc nhất có thể cho UI hiển thị qua useToast().
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const backendMessage = error.response?.data?.message;

    if (status === 401) {
      // Token hết hạn / không hợp lệ. Backend chưa có endpoint refresh,
      // nên xử lý đơn giản là phát event để AuthContext tự logout + điều hướng.
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
