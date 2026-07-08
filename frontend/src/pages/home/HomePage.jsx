import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { ROUTES } from "../../constants/routes";
import "./home.css";

export default function HomePage() {
  const { isAuthenticated } = useAuth();

  return (
    <div>
      <section className="home-hero">
        <p className="home-hero__brand">HiveMind · AI Study Hub</p>
        <h1 className="home-hero__title">
          Lưu trữ tài liệu học tập, hỏi AI, học cùng nhau.
        </h1>
        <p className="home-hero__subtitle">
          HiveMind giúp bạn tải lên, tìm kiếm và đánh giá tài liệu môn học, đồng
          thời trò chuyện trực tiếp với AI HiveMind để giải đáp nhanh các thắc mắc.
        </p>
        <div className="home-hero__actions">
          {isAuthenticated ? (
            <Link to={ROUTES.DASHBOARD} className="home-btn home-btn--primary">
              Vào Dashboard
            </Link>
          ) : (
            <>
              <Link to={ROUTES.SIGNUP} className="home-btn home-btn--primary">
                Bắt đầu miễn phí
              </Link>
              <Link to={ROUTES.LOGIN} className="home-btn home-btn--ghost">
                Đăng nhập
              </Link>
            </>
          )}
        </div>
      </section>

      <section className="home-features">
        <div className="home-feature">
          <p className="home-feature__icon">📚</p>
          <h3 className="home-feature__title">Kho tài liệu</h3>
          <p className="home-feature__desc">
            Tải lên, tìm kiếm và lọc tài liệu theo môn học, gắn tag để dễ
            quản lý.
          </p>
        </div>
        <div className="home-feature">
          <p className="home-feature__icon">🤖</p>
          <h3 className="home-feature__title">Hỏi AI HiveMind</h3>
          <p className="home-feature__desc">
            Đặt câu hỏi cho AI HiveMind, có thể hỏi riêng về nội dung của một tài
            liệu cụ thể.
          </p>
        </div>
        <div className="home-feature">
          <p className="home-feature__icon">⭐</p>
          <h3 className="home-feature__title">Cộng đồng đánh giá</h3>
          <p className="home-feature__desc">
            Yêu thích, bình luận và đánh giá tài liệu để cùng nhau chọn lọc
            nguồn học tốt nhất.
          </p>
        </div>
      </section>
    </div>
  );
}
