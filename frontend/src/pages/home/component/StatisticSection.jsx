import { useEffect, useRef, useState } from "react";
import "./StatisticSection.css";

// =============================================
// DATA — thay bằng props hoặc API call sau này
// =============================================

const SECTION_HEADER = {
  badge: "✦ Con số biết nói",
  headline: "Nền tảng đang",
  headlineAccent: "lớn mạnh",
  headlineSuffix: "từng ngày",
  subtext:
    "Hàng nghìn sinh viên tin tưởng và sử dụng AI Study Hub mỗi ngày để học hiệu quả hơn.",
};

// Mỗi card có: icon (Tabler), label, value, suffix, desc, và bars[]
// bars[]: { label, pct, variant } — variant: "primary" | "accent" | "danger"
const STAT_CARDS = [
  {
    id: "documents",
    icon: "ti-files",
    label: "Tổng tài liệu",
    value: "12",
    suffix: "K+",
    desc: "Phân bổ theo môn học phổ biến",
    bars: [
      { label: "Toán - Lý", pct: 78, variant: "primary" },
      { label: "CNTT",      pct: 64, variant: "primary" },
      { label: "Kinh tế",   pct: 51, variant: "accent"  },
      { label: "Y - Dược",  pct: 38, variant: "accent"  },
    ],
  },
  {
    id: "students",
    icon: "ti-users",
    label: "Sinh viên hoạt động",
    value: "8",
    suffix: "K+",
    desc: "Theo trường đại học",
    bars: [
      { label: "Bách Khoa", pct: 82, variant: "primary" },
      { label: "ĐH CNTT",   pct: 69, variant: "primary" },
      { label: "Kinh tế",   pct: 55, variant: "accent"  },
      { label: "Khác",      pct: 41, variant: "accent"  },
    ],
  },
  {
    id: "subjects",
    icon: "ti-book",
    label: "Môn học được cover",
    value: "120",
    suffix: "+",
    desc: "Mức độ hoàn thiện nội dung",
    bars: [
      { label: "Cơ sở ngành",  pct: 91, variant: "primary" },
      { label: "Đại cương",    pct: 88, variant: "primary" },
      { label: "Chuyên ngành", pct: 73, variant: "primary" },
      { label: "Ngoại ngữ",   pct: 45, variant: "accent"  },
    ],
  },
  {
    id: "satisfaction",
    icon: "ti-star",
    label: "Tỷ lệ hài lòng",
    value: "98",
    suffix: "%",
    desc: "Đánh giá từ sinh viên",
    bars: [
      { label: "5 sao",  pct: 72, variant: "primary" },
      { label: "4 sao",  pct: 26, variant: "primary" },
      { label: "3 sao",  pct: 2,  variant: "accent"  },
      { label: "1–2 sao",pct: 0,  variant: "danger"  },
    ],
  },
];

// =============================================

function StatBar({ label, pct, variant, animate }) {
  return (
    <div className="stat-bar-row">
      <span className="stat-bar__label">{label}</span>
      <div className="stat-bar__bg">
        <div
          className={`stat-bar__fill stat-bar__fill--${variant}`}
          style={{ width: animate ? `${pct}%` : "0%" }}
        />
      </div>
      <span className="stat-bar__val">{pct}%</span>
    </div>
  );
}

function StatCard({ icon, label, value, suffix, desc, bars, animate }) {
  return (
    <div className="stat-card">
      <div className="stat-card__top">
        <div>
          <p className="stat-card__label">{label}</p>
          <p className="stat-card__num">
            {value}<span className="stat-card__suffix">{suffix}</span>
          </p>
        </div>
        <div className="stat-card__icon" aria-hidden="true">
          <i className={`ti ${icon}`} />
        </div>
      </div>
      <p className="stat-card__desc">{desc}</p>
      <div className="stat-bars">
        {bars.map((bar) => (
          <StatBar key={bar.label} {...bar} animate={animate} />
        ))}
      </div>
    </div>
  );
}

function StatisticSection() {
  const [animate, setAnimate] = useState(false);
  const sectionRef = useRef(null);

  // Trigger bar animation khi section scroll vào viewport
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => { if (entry.isIntersecting) setAnimate(true); },
      { threshold: 0.2 }
    );
    if (sectionRef.current) observer.observe(sectionRef.current);
    return () => observer.disconnect();
  }, []);

  return (
    <section id="statisticSection" className="statistic-section" ref={sectionRef}>

      <div className="statistic-header">
        <div className="statistic-header__left">
          <span className="statistic-badge">{SECTION_HEADER.badge}</span>
          <h2 className="statistic-h2">
            {SECTION_HEADER.headline}{" "}
            <span className="statistic-h2__accent">{SECTION_HEADER.headlineAccent}</span>
            <br />{SECTION_HEADER.headlineSuffix}
          </h2>
        </div>
        <p className="statistic-sub">{SECTION_HEADER.subtext}</p>
      </div>

      <div className="statistic-grid">
        {STAT_CARDS.map((card) => (
          <StatCard key={card.id} {...card} animate={animate} />
        ))}
      </div>

    </section>
  );
}

export default StatisticSection;