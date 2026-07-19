import { Link } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import imgLibrary from "../../assets/images/feature-library.png";
import imgCommunity from "../../assets/images/feature-community.png";

const BLOCKS = [
  { chip: "Quản lý tri thức", icon: "📚", title: "Kho tài liệu thông minh", desc: "Hệ thống lưu trữ tối ưu cho học thuật, giúp bạn sắp xếp hàng ngàn tài liệu chỉ trong vài giây.", bullets: ["Tải lên đa định dạng (PDF, DOCX, PPTX) với dung lượng lớn.", "Tự động phân loại theo môn học và nhãn dán thông minh.", "Tìm kiếm nội dung sâu bên trong từng trang tài liệu."], image: imgLibrary },
  { chip: "Trợ lý AI", icon: "🤖", title: "Hỏi AI HiveMind", desc: "Trợ lý AI đồng hành, hiểu nội dung tài liệu của bạn và trả lời tức thì.", bullets: ["Hỏi đáp trực tiếp dựa trên nội dung tài liệu cụ thể.", "Tóm tắt chương dài thành các ý chính cô đọng.", "Gợi ý câu hỏi ôn tập dựa trên kiến thức trong file."], image: null },
  { chip: "Kết nối", icon: "👥", title: "Cộng đồng chia sẻ", desc: "Học cùng nhau, chọn lọc nguồn tài liệu tốt nhất từ cộng đồng.", bullets: ["Đánh giá và nhận xét tài liệu từ người dùng uy tín.", "Lưu tài liệu công khai về kho cá nhân chỉ với 1 chạm.", "Khám phá các bộ sưu tập tài liệu được biên soạn kỹ lưỡng."], image: imgCommunity },
  { chip: "Tổ chức", icon: "🗂️", title: "Không gian lưu trữ (Spaces)", desc: "Tạo không gian riêng cho từng kỳ học hoặc chủ đề và cùng bạn bè học tập.", bullets: ["Tạo không gian riêng biệt cho từng kỳ học hoặc chủ đề.", "Chia sẻ nguyên một Không gian cho bạn bè cùng học.", "Thống kê tiến độ đọc và học trong từng không gian."], image: null },
];

export default function FeaturesPage() {
  return (
    <div className="text-app">
      <section className="max-w-7xl mx-auto px-6 pt-16 pb-8 text-center">
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight">Tính năng nổi bật</h1>
        <p className="text-secondary text-lg max-w-2xl mx-auto mt-4">Khám phá hệ sinh thái học tập thông minh, nơi AI và cộng đồng cùng nhau kiến tạo tri thức bền vững.</p>
      </section>
      <div className="max-w-7xl mx-auto px-6 py-12 space-y-20">
        {BLOCKS.map((b, i) => (
          <div key={b.title} className={"flex flex-col gap-10 items-center md:flex-row" + (i % 2 ? " md:flex-row-reverse" : "")}>
            <div className="w-full md:w-1/2 space-y-5">
              <span className="inline-flex items-center gap-2 bg-surface-soft text-primary-dark rounded-pill px-4 py-1 text-sm font-semibold">{b.icon} {b.chip}</span>
              <h2 className="text-2xl md:text-3xl font-bold">{b.title}</h2>
              <p className="text-secondary">{b.desc}</p>
              <ul className="space-y-3">
                {b.bullets.map((t) => (
                  <li key={t} className="flex items-start gap-3"><span className="text-primary font-bold mt-0.5">✓</span><span>{t}</span></li>
                ))}
              </ul>
            </div>
            <div className="w-full md:w-1/2">
              {b.image ? (
                <img src={b.image} alt={b.title} className="w-full rounded-card border border-border shadow-md" />
              ) : (
                <div className="w-full aspect-video rounded-card bg-surface-soft border border-border flex items-center justify-center text-6xl">{b.icon}</div>
              )}
            </div>
          </div>
        ))}
      </div>
      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="bg-surface-soft rounded-card p-8 md:p-14 text-center">
          <h2 className="text-3xl font-bold text-primary-dark">Sẵn sàng để học thông minh hơn?</h2>
          <p className="text-secondary text-lg mt-3 mb-6">Tạo tài khoản miễn phí và bắt đầu xây dựng kho tri thức của bạn.</p>
          <Link to={ROUTES.SIGNUP} className="inline-block bg-primary hover:bg-primary-dark text-white px-8 py-4 rounded-pill font-bold transition-colors">Đăng ký miễn phí</Link>
        </div>
      </section>
    </div>
  );
}
