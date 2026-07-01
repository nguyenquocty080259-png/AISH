import { Link } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import "./notfound.css";

export default function NotFoundPage() {
  return (
    <div className="notfound">
      <p className="notfound__code">404</p>
      <h1 className="notfound__title">Không tìm thấy trang</h1>
      <p className="notfound__desc">
        Trang bạn vừa truy cập không tồn tại hoặc đã bị di chuyển.
      </p>
      <Link to={ROUTES.HOME} className="notfound__link">
        Về trang chủ
      </Link>
    </div>
  );
}
