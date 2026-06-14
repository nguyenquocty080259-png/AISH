import "./FeatureSection.css";

// =============================================
// DATA — thay bằng props hoặc API call sau này
// =============================================

const SECTION_HEADER = {
  badge: "✦ Tính năng nổi bật",
  headline: "Mọi thứ bạn cần để",
  headlineAccent: "học hiệu quả hơn",
  subtext:
    "Từ tìm kiếm thông minh đến học cùng AI — AI Study Hub trang bị đầy đủ công cụ cho hành trình học tập của bạn.",
};

// large: true => card chiếm 2 cột
// Layout 3 hàng, mỗi hàng = 3 cột:
// Hàng 1: large(2) + nhỏ(1)
// Hàng 2: nhỏ(1)  + large(2)
// Hàng 3: large(2) + nhỏ(1)
const FEATURES = [
  // — hàng 1 —
  {
    id: "ai-search",
    icon: "🔍",
    title: "Tìm kiếm tài liệu AI",
    desc: "AI hiểu ngữ nghĩa câu hỏi của bạn — không chỉ tìm từ khóa mà còn gợi ý tài liệu liên quan, đúng môn học và cấp độ.",
    tags: ["Semantic search", "Gợi ý thông minh", "Lọc theo môn"],
    large: true,
    href: "#",
  },
  {
    id: "summarize",
    icon: "📝",
    title: "Tóm tắt nội dung",
    desc: "Upload tài liệu dài, AI tóm tắt thành ghi chú ngắn gọn theo từng chương, phù hợp để ôn thi.",
    tags: ["PDF / DOCX", "Theo chương"],
    large: false,
    href: "#",
  },
  // — hàng 2 —
  {
    id: "progress",
    icon: "📊",
    title: "Quản lý tiến độ học",
    desc: "Theo dõi từng môn học, đặt mục tiêu theo tuần và nhận báo cáo tiến độ chi tiết.",
    tags: ["Dashboard", "Mục tiêu tuần"],
    large: false,
    href: "#",
  },
  {
    id: "share",
    icon: "🔗",
    title: "Chia sẻ tài liệu",
    desc: "Chia sẻ ghi chú và tài liệu với bạn bè, tạo thư viện học nhóm hoặc đóng góp cho cộng đồng sinh viên toàn quốc.",
    tags: ["Học nhóm", "Cộng đồng", "Public / Private"],
    large: true,
    href: "#",
  },
  // — hàng 3 —
  {
    id: "chatbot",
    icon: "🤖",
    title: "Học cùng AI chatbot",
    desc: "Đặt câu hỏi về bất kỳ tài liệu nào, AI giải thích ngay lập tức như một gia sư riêng 24/7.",
    tags: ["Q&A tức thì", "24/7"],
    large: true,
    href: "#",
  },
  {
    id: "flashcard",
    icon: "🃏",
    title: "Flashcard ôn tập",
    desc: "Tự động tạo flashcard từ tài liệu, ôn luyện theo phương pháp lặp lại ngắt quãng (spaced repetition).",
    tags: ["Spaced repetition", "Auto-generate"],
    large: false,
    href: "#",
  },
];

// =============================================

function FeatureCard({ icon, title, desc, tags, large, href }) {
  return (
    <a
      href={href}
      className={`feature-card${large ? " feature-card--large" : ""}`}
      aria-label={title}
    >
      <span className="feature-card__arrow" aria-hidden="true">↗</span>
      <div className="feature-card__icon">{icon}</div>
      <h3 className="feature-card__title">{title}</h3>
      <p className="feature-card__desc">{desc}</p>
      <div className="feature-card__tags">
        {tags.map((tag) => (
          <span key={tag} className="feature-card__tag">{tag}</span>
        ))}
      </div>
    </a>
  );
}

function FeatureSection() {
  return (
    <section id="featureSection" className="feature-section">

      <div className="feature-header">
        <span className="feature-badge">{SECTION_HEADER.badge}</span>
        <h2 className="feature-h2">
          {SECTION_HEADER.headline}<br />
          <span className="feature-h2__accent">{SECTION_HEADER.headlineAccent}</span>
        </h2>
        <p className="feature-sub">{SECTION_HEADER.subtext}</p>
      </div>

      <div className="feature-grid">
        {FEATURES.map((feature) => (
          <FeatureCard key={feature.id} {...feature} />
        ))}
      </div>

    </section>
  );
}

export default FeatureSection;