import { Link } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import img1 from "../../assets/images/how-1-upload.png";
import img2 from "../../assets/images/how-2-categorize.png";
import img3 from "../../assets/images/how-3-collaborate.png";
import img4 from "../../assets/images/how-4-ai-chat.png";

const STEPS = [
  { n: 1, icon: "📤", title: "Tải tài liệu lên", desc: "Tải lên mọi định dạng tài liệu học tập: PDF, DOCX, hoặc hình ảnh bài giảng. HiveMind sẽ tự động trích xuất nội dung và sẵn sàng xử lý.", image: img1 },
  { n: 2, icon: "📚", title: "Phân loại theo môn học", desc: "Hệ thống đề xuất môn học phù hợp cho tài liệu của bạn. Tổ chức kho tri thức của riêng bạn một cách khoa học nhất.", image: img2 },
  { n: 3, icon: "👥", title: "Chia sẻ với cộng đồng", desc: "Kết nối với tổ ong tri thức. Chia sẻ tài liệu của bạn hoặc tham khảo tài liệu từ hàng ngàn người học khác để mở rộng góc nhìn.", image: img3 },
  { n: 4, icon: "🤖", title: "Hỏi AI về nội dung tài liệu", desc: "Trò chuyện trực tiếp với tài liệu của bạn. Yêu cầu tóm tắt, giải thích khái niệm khó hoặc đặt câu hỏi tự kiểm tra kiến thức.", image: img4 },
];

export default function HowItWorksPage() {
  return (
    <div className="text-app">
      <section className="max-w-7xl mx-auto px-6 pt-16 pb-8 text-center">
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight">Cách HiveMind hoạt động</h1>
        <p className="text-secondary text-lg max-w-2xl mx-auto mt-4">Tối ưu hóa quy trình học tập của bạn với sự hỗ trợ từ trí tuệ nhân tạo. Đơn giản, hiệu quả và được thiết kế cho người học hiện đại.</p>
      </section>
      <div className="max-w-7xl mx-auto px-6 py-12 space-y-16">
        {STEPS.map((s, i) => (
          <div key={s.n} className={"flex flex-col gap-8 items-center md:flex-row" + (i % 2 ? " md:flex-row-reverse" : "")}>
            <div className="w-full md:w-1/2">
              <div className="flex items-start gap-5">
                <div className="shrink-0 w-14 h-14 bg-primary text-white rounded-full flex items-center justify-center font-bold text-xl shadow-sm">{s.n}</div>
                <div>
                  <div className="text-3xl mb-2">{s.icon}</div>
                  <h3 className="text-xl font-bold mb-2">{s.title}</h3>
                  <p className="text-secondary">{s.desc}</p>
                </div>
              </div>
            </div>
            <div className="w-full md:w-1/2">
              <img src={s.image} alt={s.title} className="w-full rounded-card border border-border shadow-md" />
            </div>
          </div>
        ))}
      </div>
      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="bg-surface-soft rounded-card p-8 md:p-14 text-center">
          <h2 className="text-3xl font-bold text-primary-dark">Bắt đầu hành trình học tập thông minh ngay</h2>
          <p className="text-secondary text-lg mt-3 mb-6">Chỉ vài bước đơn giản để biến tài liệu thành tri thức.</p>
          <Link to={ROUTES.SIGNUP} className="inline-block bg-primary hover:bg-primary-dark text-white px-8 py-4 rounded-pill font-bold transition-colors">Đăng ký miễn phí</Link>
        </div>
      </section>
    </div>
  );
}
