import { useAuth } from "../../context/AuthContext";
import "./Dashboardpage.css";

// ============================================================
// DATA — thay bằng API call sau này
// Ví dụ: const { data } = useSWR(`/api/student/${user.id}/dashboard`)
// ============================================================

const MOCK_STATS = [
  { id: "docs",      icon: "📄", label: "Tài liệu đã học",   value: "24",  suffix: "",  sub: "+3 tuần này"        },
  { id: "hours",     icon: "⏱",  label: "Giờ học tháng này", value: "18",  suffix: "h", sub: "Trung bình 36ph/ngày"},
  { id: "flashcard", icon: "🃏", label: "Flashcard đã ôn",   value: "142", suffix: "",  sub: "12 thẻ chờ ôn"      },
  { id: "ai",        icon: "🤖", label: "Câu hỏi AI",        value: "56",  suffix: "",  sub: "Tháng này"           },
];

const MOCK_PROGRESS = [
  { subject: "Giải tích 1",      pct: 85, color: "#E08307" },
  { subject: "Lập trình Python", pct: 72, color: "#E08307" },
  { subject: "Kinh tế vi mô",   pct: 58, color: "#E8B631" },
  { subject: "Vật lý ĐC 2",     pct: 41, color: "#E8B631" },
];

const MOCK_RECENT_DOCS = [
  { emoji: "📐", bg: "#FFF8EE", name: "Giải tích 1 — Chương 3",    meta: "Hôm nay · 24 trang",      tag: "PDF"  },
  { emoji: "💻", bg: "#EEF8FF", name: "Python — OOP nâng cao",      meta: "Hôm qua · 18 trang",      tag: "PDF"  },
  { emoji: "📈", bg: "#FFF4EE", name: "Kinh tế vi mô — Đề cương",   meta: "2 ngày trước · 12 trang", tag: "DOCX" },
  { emoji: "⚡", bg: "#F4EEFF", name: "Vật lý ĐC 2 — Điện từ học", meta: "3 ngày trước · 30 trang", tag: "PDF"  },
];

const MOCK_FLASHCARDS = [
  { emoji: "📐", bg: "#FFF8EE", subject: "Giải tích 1",   due: "Hôm nay",  count: 5 },
  { emoji: "💻", bg: "#EEF8FF", subject: "Python cơ bản", due: "Hôm nay",  count: 4 },
  { emoji: "📈", bg: "#FFF4EE", subject: "Kinh tế vi mô", due: "Ngày mai", count: 3 },
];

const AI_SUGGESTIONS = [
  { icon: "📝", label: "Tóm tắt tài liệu mới"   },
  { icon: "❓", label: "Hỏi về bài học hôm nay"  },
  { icon: "🃏", label: "Tạo flashcard từ PDF"    },
];

const STREAK_DAYS = 7; // TODO: lấy từ API

// ============================================================

function StatCard({ icon, label, value, suffix, sub }) {
  return (
    <div className="stat-card">
      <div className="stat-card-top">
        <div className="stat-icon">{icon}</div>
      </div>
      <div className="stat-value">{value}<span className="stat-suffix">{suffix}</span></div>
      <div className="stat-label">{label}</div>
      <div className="stat-sub">{sub}</div>
    </div>
  );
}

function ProgressBar({ subject, pct, color }) {
  return (
    <div className="prog-row">
      <div className="prog-meta">
        <span className="prog-subject">{subject}</span>
        <span className="prog-pct">{pct}%</span>
      </div>
      <div className="prog-bg">
        <div className="prog-fill" style={{ width: `${pct}%`, background: color }} />
      </div>
    </div>
  );
}

export default function StudentDashboard() {
  const { user } = useAuth();

  return (
    <div className="main-content">

      {/* Welcome */}
      <div className="welcome-row">
        <div>
          <h1 className="welcome-h1">Chào buổi sáng, {user?.name ?? "bạn"}! 👋</h1>
          <p className="welcome-sub">
            {user?.school} · Hôm nay có <b>3 flashcard</b> cần ôn và <b>2 tài liệu</b> chưa đọc xong.
          </p>
        </div>
        <div className="streak-badge">
          <span style={{ fontSize: 22 }}>🔥</span>
          <div>
            <div className="streak-num">{STREAK_DAYS} ngày</div>
            <div className="streak-label">liên tiếp</div>
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="stat-grid">
        {MOCK_STATS.map(s => <StatCard key={s.id} {...s} />)}
      </div>

      {/* Content */}
      <div className="content-grid">

        <div className="col-left">
          <div className="card">
            <div className="card-head">
              <span className="card-title">Tiến độ môn học</span>
              <button className="card-link">Xem tất cả</button>
            </div>
            <div className="prog-list">
              {MOCK_PROGRESS.map(p => <ProgressBar key={p.subject} {...p} />)}
            </div>
          </div>

          <div className="card">
            <div className="card-head">
              <span className="card-title">Tài liệu gần đây</span>
              <button className="card-link">Xem tất cả</button>
            </div>
            <div className="doc-list">
              {MOCK_RECENT_DOCS.map((d, i) => (
                <div className="doc-item" key={i}>
                  <div className="doc-thumb" style={{ background: d.bg }}>{d.emoji}</div>
                  <div className="doc-info">
                    <div className="doc-name">{d.name}</div>
                    <div className="doc-meta">{d.meta}</div>
                  </div>
                  <span className="doc-tag">{d.tag}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="col-right">
          <div className="card">
            <div className="card-head"><span className="card-title">AI Study</span></div>
            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              {AI_SUGGESTIONS.map((s, i) => (
                <div className="ai-chip" key={i}>
                  <span>{s.icon}</span> {s.label}
                </div>
              ))}
              <button className="ai-cta">🤖 Mở AI chatbot</button>
            </div>
          </div>

          <div className="card">
            <div className="card-head">
              <span className="card-title">Flashcard cần ôn</span>
              <button className="card-link">Ôn ngay</button>
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              {MOCK_FLASHCARDS.map((f, i) => (
                <div className="flash-item" key={i}>
                  <div className="flash-icon" style={{ background: f.bg }}>{f.emoji}</div>
                  <div className="flash-info">
                    <div className="flash-name">{f.subject}</div>
                    <div className="flash-due">{f.due}</div>
                  </div>
                  <span className="flash-count">{f.count} thẻ</span>
                </div>
              ))}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
