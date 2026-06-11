import "./HeroSection.css";

// =============================================
// DATA — thay bằng props hoặc API call sau này
// =============================================

const HERO_CONTENT = {
  badge: "✦ AI-powered learning platform",
  headline: "Học thông minh hơn",
  headlineAccent: "AI Study Hub",
  subtext:
    "Tìm kiếm tài liệu, tóm tắt nội dung và học cùng AI — tất cả trong một nền tảng dành cho sinh viên Việt Nam.",
  searchPlaceholder: "Tìm tài liệu, môn học, chủ đề...",
  searchBtnLabel: "Tìm kiếm",
  aiFloatLabel: "🤖 AI đang tóm tắt...",
};

const POPULAR_TAGS = [
  "Toán cao cấp",
  "Lập trình Python",
  "Kinh tế vi mô",
  "Vật lý đại cương",
];

const HERO_STATS = [
  { value: "12K", suffix: "+", label: "Tài liệu" },
  { value: "8K",  suffix: "+", label: "Sinh viên" },
  { value: "98",  suffix: "%", label: "Hài lòng" },
];

const PROGRESS_SUBJECTS = [
  { label: "Toán", pct: 85, accent: false },
  { label: "Lý",   pct: 62, accent: false },
  { label: "Hóa",  pct: 73, accent: true  },
];

const DAILY_STAT = {
  avatarLetter: "A",
  viewCount: "2,400",
  viewLabel: "tài liệu đã xem hôm nay",
};

// =============================================

function HeroSection() {
  return (
    <section id="heroSection" className="hero-section">

      {/* Decorative bg blobs */}
      <div className="hero-bg-blob hero-bg-blob--1" aria-hidden="true" />
      <div className="hero-bg-blob hero-bg-blob--2" aria-hidden="true" />

      {/* Left — text + search */}
      <div className="hero-left">
        <span className="hero-badge">{HERO_CONTENT.badge}</span>

        <h1 className="hero-h1">
          {HERO_CONTENT.headline}<br />
          với <span className="hero-h1__accent">{HERO_CONTENT.headlineAccent}</span>
        </h1>

        <p className="hero-sub">{HERO_CONTENT.subtext}</p>

        <div className="hero-search">
          <svg className="hero-search__icon" width="18" height="18" viewBox="0 0 24 24"
            fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
            aria-hidden="true">
            <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            className="hero-search__input"
            placeholder={HERO_CONTENT.searchPlaceholder}
            aria-label={HERO_CONTENT.searchPlaceholder}
          />
          <button className="hero-search__btn" type="button">
            {HERO_CONTENT.searchBtnLabel}
          </button>
        </div>

        <div className="hero-tags" aria-label="Chủ đề phổ biến">
          {POPULAR_TAGS.map((tag) => (
            <button key={tag} className="hero-tag" type="button">
              {tag}
            </button>
          ))}
        </div>

        <div className="hero-stats">
          {HERO_STATS.map(({ value, suffix, label }) => (
            <div className="hero-stat" key={label}>
              <span className="hero-stat__num">
                {value}<span className="hero-stat__suffix">{suffix}</span>
              </span>
              <span className="hero-stat__label">{label}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Right — illustration cards */}
      <div className="hero-right" aria-hidden="true">
        <div className="hero-illus">

          <div className="illus-float">{HERO_CONTENT.aiFloatLabel}</div>

          <div className="illus-card">
            <div className="illus-card__header">
              <div className="illus-icon">📄</div>
              <div>
                <p className="illus-card__title">Tiến độ học tập</p>
                <p className="illus-card__sub">Tuần này · {PROGRESS_SUBJECTS.length} môn</p>
              </div>
            </div>
            <div className="illus-bars">
              {PROGRESS_SUBJECTS.map(({ label, pct, accent }) => (
                <div className="illus-bar-row" key={label}>
                  <span className="illus-bar__label">{label}</span>
                  <div className="illus-bar__bg">
                    <div
                      className={`illus-bar__fill${accent ? " illus-bar__fill--accent" : ""}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                  <span className="illus-bar__val">{pct}%</span>
                </div>
              ))}
            </div>
          </div>

          <div className="illus-card illus-card--row">
            <div className="illus-avatar">{DAILY_STAT.avatarLetter}</div>
            <div className="illus-card__body">
              <div className="illus-stat__num">{DAILY_STAT.viewCount}</div>
              <div className="illus-stat__label">{DAILY_STAT.viewLabel}</div>
            </div>
            <span className="illus-trend">↗</span>
          </div>

        </div>
      </div>

    </section>
  );
}

export default HeroSection;