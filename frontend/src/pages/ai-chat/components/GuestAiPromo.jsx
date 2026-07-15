import { Link } from "react-router-dom";
import { ROUTES } from "../../../constants/routes";

export default function GuestAiPromo() {
  return (
    <main className="guest-ai-promo">
      <section className="guest-ai-promo__card">
        <div className="guest-ai-promo__media" aria-label="Khung video giới thiệu AI HiveMind">
          <span className="guest-ai-promo__play" aria-hidden="true" />
          <p>Video giới thiệu sắp ra mắt</p>
        </div>

        <div className="guest-ai-promo__content">
          <span className="guest-ai-promo__eyebrow">AI HiveMind</span>
          <h1>Học sâu hơn với trợ lý AI của bạn</h1>
          <p className="guest-ai-promo__intro">
            Biến tài liệu học tập thành những cuộc trò chuyện hữu ích, có căn cứ và dễ theo dõi.
          </p>

          <ul className="guest-ai-promo__features">
            <li>Trò chuyện trực tiếp với nội dung trong tài liệu của bạn.</li>
            <li>Kiểm chứng câu trả lời qua trích dẫn theo từng trang.</li>
            <li>Tiếp tục mạch học nhờ ghi nhớ lịch sử hội thoại.</li>
            <li>Khám phá thêm kiến thức qua các tài liệu liên quan.</li>
          </ul>

          <div className="guest-ai-promo__actions">
            <Link to={ROUTES.LOGIN} className="guest-ai-promo__button guest-ai-promo__button--primary">
              Đăng nhập để dùng thử
            </Link>
            <Link to={ROUTES.SIGNUP} className="guest-ai-promo__button guest-ai-promo__button--secondary">
              Đăng ký
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
