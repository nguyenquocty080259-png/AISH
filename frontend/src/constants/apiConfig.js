// Origin của backend cho những URL KHÔNG đi qua apiClient: ảnh đại diện và file tĩnh do BE
// phục vụ trực tiếp (/uploads/...). Các trang đang tự nối chuỗi "http://localhost:8080" nên
// build production trỏ thẳng về máy người dùng và ảnh vỡ hết.
// Đặt VITE_API_ORIGIN lúc build để đổi theo môi trường; mặc định là BE chạy local khi dev.
export const API_ORIGIN =
  import.meta.env.VITE_API_ORIGIN || "http://localhost:8080";
