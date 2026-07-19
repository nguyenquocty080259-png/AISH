import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { ROUTES } from "../../constants/routes";
import heroImg from "../../assets/images/hero-honeycomb.png";

const FEATURES = [
  { icon: "📚", title: "Kho tài liệu", desc: "Lưu trữ và quản lý hàng ngàn tài liệu học tập một cách khoa học theo từng chủ đề và không gian riêng biệt." },
  { icon: "🤖", title: "Hỏi AI HiveMind", desc: "Trợ lý AI thông minh sẵn sàng giải đáp thắc mắc, tóm tắt nội dung và tạo đề thi thử từ tài liệu của chính bạn." },
  { icon: "👥", title: "Cộng đồng chia sẻ", desc: "Kết nối với những người cùng đam mê, chia sẻ tài liệu hữu ích và cùng nhau thảo luận để tiến bộ mỗi ngày." },
];

const STEPS = [
  { n: 1, title: "Tải lên", desc: "Tải file PDF, DOCX hoặc ghi chú trực tiếp vào hệ thống.", filled: false },
  { n: 2, title: "Phân loại", desc: "Sắp xếp và gắn tag từ khóa cho tài liệu của bạn.", filled: false },
  { n: 3, title: "Chia sẻ & hỏi AI", desc: "Tương tác với tài liệu qua chat AI và mời bạn bè cùng học.", filled: true },
];

export default function HomePage() {
  const { isAuthenticated } = useAuth();
  return (
    <div className="text-app">
      <section className="max-w-7xl mx-auto px-6 pt-16 pb-24 flex flex-col md:flex-row items-center gap-12">
        <div className="w-full md:w-1/2 space-y-6">
          <span className="inline-flex items-center gap-2 bg-surface-soft text-primary-dark border border-primary rounded-pill px-4 py-1.5 text-xs font-semibold uppercase tracking-wider">✨ HiveMind · AI Study Hub</span>
          <h1 className="text-4xl md:text-5xl font-bold leading-tight tracking-tight">Học nhóm thông minh hơn cùng kho tài liệu và AI</h1>
          <p className="text-secondary text-lg max-w-xl">Biến đổi cách bạn học tập với trợ lý AI thông minh, tự động tóm tắt kiến thức và kết nối cộng đồng sinh viên năng động.</p>
          <div className="flex flex-col sm:flex-row gap-4 pt-2">
            <Link to={isAuthenticated ? ROUTES.DASHBOARD : ROUTES.SIGNUP} className="bg-primary hover:bg-primary-dark text-white px-8 py-4 rounded-input font-bold text-center transition-colors">
              {isAuthenticated ? "Vào Dashboard" : "Bắt đầu ngay"}
            </Link>
            <Link to={ROUTES.FEATURES} className="bg-surface border border-border hover:border-primary text-app px-8 py-4 rounded-input font-semibold text-center transition-colors">Khám phá tính năng</Link>
          </div>
        </div>
        <div className="w-full md:w-1/2">
          <div className="relative max-w-[480px] mx-auto">
            <div className="absolute inset-0 bg-surface-soft rounded-card scale-105 -z-10"></div>
            <img src={heroImg} alt="HiveMind" className="w-full rounded-card border border-border shadow-lg" />
          </div>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="flex justify-between items-end mb-10">
          <div>
            <h2 className="text-3xl font-bold">Tính năng nổi bật</h2>
            <p className="text-secondary mt-2">Mọi công cụ bạn cần để tối ưu hóa quá trình tiếp thu kiến thức.</p>
          </div>
          <Link to={ROUTES.FEATURES} className="text-primary text-sm font-semibold uppercase tracking-widest hover:text-primary-dark whitespace-nowrap">Xem thêm ›</Link>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {FEATURES.map((f) => (
            <div key={f.title} className="bg-surface p-8 rounded-card border border-border shadow-sm transition-transform hover:-translate-y-2">
              <div className="w-14 h-14 bg-surface-soft rounded-2xl flex items-center justify-center text-2xl mb-6">{f.icon}</div>
              <h3 className="text-lg font-semibold mb-3">{f.title}</h3>
              <p className="text-secondary text-sm leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-surface-soft py-20">
        <div className="max-w-7xl mx-auto px-6 text-center">
          <div className="max-w-2xl mx-auto mb-14">
            <h2 className="text-3xl font-bold">Cách hoạt động</h2>
            <p className="text-secondary mt-3">Đơn giản hóa quy trình học tập của bạn chỉ trong 3 bước nhanh chóng.</p>
          </div>
          <div className="flex flex-col md:flex-row justify-between items-start gap-12">
            {STEPS.map((s) => (
              <div key={s.n} className="flex-1 flex flex-col items-center">
                <div className={(s.filled ? "bg-primary text-white " : "bg-surface text-primary ") + "w-16 h-16 border-2 border-primary rounded-full flex items-center justify-center font-bold text-xl mb-6 shadow-sm"}>{s.n}</div>
                <h4 className="text-lg font-semibold mb-2">{s.title}</h4>
                <p className="text-secondary text-sm max-w-[240px]">{s.desc}</p>
              </div>
            ))}
          </div>
          <div className="mt-14">
            <Link to={ROUTES.HOW_IT_WORKS} className="text-primary font-semibold hover:text-primary-dark">Xem chi tiết quy trình →</Link>
          </div>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="bg-surface-soft rounded-card p-8 md:p-16 flex flex-col md:flex-row items-center justify-between gap-8 text-center md:text-left">
          <div>
            <h2 className="text-3xl md:text-4xl font-bold text-primary-dark">Sẵn sàng bắt đầu?</h2>
            <p className="text-secondary text-lg mt-3">Tham gia cộng đồng HiveMind và tối ưu việc học của bạn ngay hôm nay.</p>
          </div>
          <Link to={isAuthenticated ? ROUTES.DASHBOARD : ROUTES.SIGNUP} className="bg-primary hover:bg-primary-dark text-white px-10 py-5 rounded-pill font-bold whitespace-nowrap transition-colors">
            {isAuthenticated ? "Vào Dashboard" : "Đăng ký miễn phí"}
          </Link>
        </div>
      </section>
    </div>
  );
}
