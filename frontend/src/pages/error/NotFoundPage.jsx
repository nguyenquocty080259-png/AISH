import { Link } from "react-router-dom";
import "./NotFoundPage.css";

export default function NotFoundPage() {
  return (
    <div className="notfound">
      <div className="notfound__content">
        <span className="notfound__code">404</span>
        <h1 className="notfound__title">Trang không tồn tại</h1>
        <p className="notfound__desc">
          Trang bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.
        </p>
        <Link to="/" className="notfound__btn">← Về trang chủ</Link>
      </div>
    </div>
  );
}