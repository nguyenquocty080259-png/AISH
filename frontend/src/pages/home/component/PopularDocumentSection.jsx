import { useState, useRef } from "react";
import "./PopularDocumentSection.css";

// =============================================
// DATA — thay bằng props hoặc API call sau này
// =============================================

const SECTION_HEADER = {
  badge: "✦ Tài liệu phổ biến",
  headline: "Được tải nhiều",
  headlineAccent: "nhất tuần này",
};

const SORT_OPTIONS = [
  { key: "views", label: "Lượt xem" },
  { key: "new",   label: "Mới nhất" },
];

// Màu nền thumbnail theo môn — tuỳ chỉnh tự do
const DOCUMENTS = [
  {
    id: 1,
    emoji: "📐",
    thumbBg: "#FFF8EE",
    type: "PDF",
    rank: 1,
    subject: "Toán cao cấp",
    title: "Giải tích 1 — Lý thuyết & Bài tập có lời giải",
    author: "Nguyễn Văn A",
    school: "ĐH Bách Khoa",
    views: "12.4K",
    downloads: "3.2K",
    href: "#",
  },
  {
    id: 2,
    emoji: "💻",
    thumbBg: "#EEF8FF",
    type: "PDF",
    rank: 2,
    subject: "Lập trình",
    title: "Python cơ bản đến nâng cao — Kèm project thực tế",
    author: "Trần Thị B",
    school: "ĐH CNTT",
    views: "9.8K",
    downloads: "2.7K",
    href: "#",
  },
  {
    id: 3,
    emoji: "⚗️",
    thumbBg: "#EEFFF4",
    type: "DOCX",
    rank: 3,
    subject: "Hóa đại cương",
    title: "Hóa học đại cương — Tóm tắt lý thuyết chương 1–5",
    author: "Lê Văn C",
    school: "ĐH Khoa học",
    views: "8.1K",
    downloads: "2.1K",
    href: "#",
  },
  {
    id: 4,
    emoji: "📈",
    thumbBg: "#FFF4EE",
    type: "PDF",
    rank: 4,
    subject: "Kinh tế",
    title: "Kinh tế vi mô — Đề cương ôn thi cuối kỳ",
    author: "Phạm Thị D",
    school: "ĐH Kinh tế",
    views: "6.5K",
    downloads: "1.8K",
    href: "#",
  },
  {
    id: 5,
    emoji: "⚡",
    thumbBg: "#F4EEFF",
    type: "PDF",
    rank: 5,
    subject: "Vật lý",
    title: "Vật lý đại cương 2 — Điện từ học toàn tập",
    author: "Hoàng Văn E",
    school: "ĐH Bách Khoa",
    views: "5.9K",
    downloads: "1.4K",
    href: "#",
  },
  {
    id: 6,
    emoji: "🧬",
    thumbBg: "#EEFAF8",
    type: "PDF",
    rank: 6,
    subject: "Sinh học",
    title: "Sinh học phân tử — Slide bài giảng đầy đủ",
    author: "Mai Thị F",
    school: "ĐH Y Dược",
    views: "4.3K",
    downloads: "980",
    href: "#",
  },
];

// Màu huy hiệu rank
const RANK_COLORS = {
  1: "#E08307",
  2: "#888780",
  3: "#993C1D",
};
const RANK_DEFAULT = "#544435";

// =============================================

function DocCard({ emoji, thumbBg, type, rank, subject, title, author, school, views, downloads, href }) {
  const rankColor = RANK_COLORS[rank] ?? RANK_DEFAULT;

  return (
    <a href={href} className="doc-card" aria-label={title}>
      <div className="doc-card__thumb" style={{ background: thumbBg }}>
        <span className="doc-card__emoji" aria-hidden="true">{emoji}</span>
        <span className="doc-card__type">{type}</span>
        <span className="doc-card__rank" style={{ background: rankColor }}>{rank}</span>
      </div>
      <div className="doc-card__body">
        <div className="doc-card__subject">{subject}</div>
        <h3 className="doc-card__title">{title}</h3>
        <div className="doc-card__author">{author} · {school}</div>
        <div className="doc-card__footer">
          <span className="doc-card__views">👁 {views}</span>
          <span className="doc-card__dl">⬇ {downloads}</span>
        </div>
      </div>
    </a>
  );
}

function PopularDocumentSection() {
  const [activeSort, setActiveSort] = useState(SORT_OPTIONS[0].key);
  const carouselRef = useRef(null);

  function scrollCarousel(dir) {
    carouselRef.current?.scrollBy({ left: dir * 252, behavior: "smooth" });
  }

  return (
    <section id="popularDocumentSection" className="popular-section">

      <div className="popular-header">
        <div className="popular-header__left">
          <span className="popular-badge">{SECTION_HEADER.badge}</span>
          <h2 className="popular-h2">
            {SECTION_HEADER.headline}<br />
            <span className="popular-h2__accent">{SECTION_HEADER.headlineAccent}</span>
          </h2>
        </div>

        <div className="popular-sort" role="group" aria-label="Sắp xếp tài liệu">
          {SORT_OPTIONS.map(({ key, label }) => (
            <button
              key={key}
              className={`sort-btn${activeSort === key ? " sort-btn--active" : ""}`}
              onClick={() => setActiveSort(key)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      <div className="popular-carousel-wrap">
        <div className="popular-carousel" ref={carouselRef}>
          {DOCUMENTS.map((doc) => (
            <DocCard key={doc.id} {...doc} />
          ))}
        </div>
      </div>

      <div className="popular-nav">
        <button className="popular-nav__btn" onClick={() => scrollCarousel(-1)} aria-label="Cuộn trái" type="button">←</button>
        <button className="popular-nav__btn" onClick={() => scrollCarousel(1)}  aria-label="Cuộn phải" type="button">→</button>
      </div>

    </section>
  );
}

export default PopularDocumentSection;