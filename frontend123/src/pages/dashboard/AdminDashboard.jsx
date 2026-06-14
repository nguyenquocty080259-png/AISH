// ============================================================
// DATA — thay bằng API call sau này
// Ví dụ: const { data } = useSWR('/api/admin/dashboard')
// ============================================================


const MOCK_STATS = [
  { id: "users",    icon: "👥", label: "Tổng sinh viên",  value: "8,241",  suffix: "",   sub: "+124 tuần này",     trend: "up"   },
  { id: "docs",     icon: "📄", label: "Tổng tài liệu",   value: "12,480", suffix: "",   sub: "+89 hôm nay",       trend: "up"   },
  { id: "ai_calls", icon: "🤖", label: "Yêu cầu AI/ngày", value: "3,920",  suffix: "",   sub: "Trung bình 7 ngày", trend: "up"   },
  { id: "storage",  icon: "💾", label: "Dung lượng dùng", value: "2.4",    suffix: "TB", sub: "60% giới hạn",      trend: "warn" },
];

const MOCK_RECENT_USERS = [
  { name: "Nguyễn Văn A", school: "ĐH Bách Khoa", joined: "10 phút trước", status: "active"   },
  { name: "Trần Thị B",   school: "ĐH CNTT",       joined: "32 phút trước", status: "active"   },
  { name: "Lê Minh C",    school: "ĐH Kinh tế",    joined: "1 giờ trước",   status: "active"   },
  { name: "Phạm Thu D",   school: "ĐH Y Dược",     joined: "2 giờ trước",   status: "inactive" },
  { name: "Hoàng Văn E",  school: "ĐH Khoa học",   joined: "3 giờ trước",   status: "active"   },
];

const MOCK_PENDING_DOCS = [
  { emoji: "📐", name: "Giải tích 2 — Toàn tập",          uploader: "Nguyễn A", size: "4.2 MB" },
  { emoji: "💻", name: "Cấu trúc dữ liệu & Giải thuật",   uploader: "Trần B",   size: "2.8 MB" },
  { emoji: "🧬", name: "Sinh học phân tử — Chương 5",      uploader: "Lê C",     size: "6.1 MB" },
];

const MOCK_SUBJECT_STATS = [
  { subject: "Toán - Lý", pct: 78, docs: "4.2K" },
  { subject: "CNTT",       pct: 64, docs: "3.1K" },
  { subject: "Kinh tế",   pct: 51, docs: "2.4K" },
  { subject: "Y - Dược",  pct: 38, docs: "1.8K" },
];

const QUICK_ACTIONS = [
  "📨 Gửi thông báo",
  "👤 Thêm tài khoản",
  "📊 Xem log hệ thống",
  "🔒 Quản lý phân quyền",
];

// ============================================================

function StatCard({ icon, label, value, suffix, sub, trend }) {
  return (
    <div className="stat-card">
      <div className="stat-card-top">
        <div className="stat-icon">{icon}</div>
        {trend === "warn" && <span className="trend-warn">⚠</span>}
        {trend === "up"   && <span className="trend-up">↑</span>}
      </div>
      <div className="stat-value">{value}<span className="stat-suffix">{suffix}</span></div>
      <div className="stat-label">{label}</div>
      <div className="stat-sub">{sub}</div>
    </div>
  );
}

export default function AdminDashboard() {
  return (
    <div className="main-content">

      {/* Welcome */}
      <div className="welcome-row">
        <div>
          <h1 className="welcome-h1">Tổng quan hệ thống 🛠</h1>
          <p className="welcome-sub">
            Cập nhật lúc {new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })} · {new Date().toLocaleDateString("vi-VN")}
          </p>
        </div>
        <button className="admin-export-btn">📥 Xuất báo cáo</button>
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
              <span className="card-title">Sinh viên mới đăng ký</span>
              <button className="card-link">Xem tất cả</button>
            </div>
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Tên</th>
                  <th>Trường</th>
                  <th>Đăng ký</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {MOCK_RECENT_USERS.map((u, i) => (
                  <tr key={i}>
                    <td><b>{u.name}</b></td>
                    <td>{u.school}</td>
                    <td className="td-meta">{u.joined}</td>
                    <td>
                      <span className={`status-badge status-${u.status}`}>
                        {u.status === "active" ? "Hoạt động" : "Chờ xác thực"}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="card">
            <div className="card-head">
              <span className="card-title">Tài liệu chờ duyệt</span>
              <button className="card-link card-link--warn">
                Duyệt ngay ({MOCK_PENDING_DOCS.length})
              </button>
            </div>
            <div className="doc-list">
              {MOCK_PENDING_DOCS.map((d, i) => (
                <div className="doc-item" key={i}>
                  <div className="doc-thumb" style={{ background: "#FFF8EE" }}>{d.emoji}</div>
                  <div className="doc-info">
                    <div className="doc-name">{d.name}</div>
                    <div className="doc-meta">Bởi {d.uploader} · {d.size}</div>
                  </div>
                  <div className="admin-actions">
                    <button className="btn-approve" aria-label="Duyệt">✓</button>
                    <button className="btn-reject"  aria-label="Từ chối">✕</button>
                  </div>
                </div>
              ))}
            </div>
          </div>

        </div>

        <div className="col-right">

          <div className="card">
            <div className="card-head"><span className="card-title">Tài liệu theo môn</span></div>
            <div className="prog-list">
              {MOCK_SUBJECT_STATS.map(s => (
                <div className="prog-row" key={s.subject}>
                  <div className="prog-meta">
                    <span className="prog-subject">{s.subject}</span>
                    <span className="prog-pct">{s.docs}</span>
                  </div>
                  <div className="prog-bg">
                    <div className="prog-fill" style={{ width: `${s.pct}%`, background: "#E08307" }} />
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="card">
            <div className="card-head"><span className="card-title">Thao tác nhanh</span></div>
            <div className="quick-actions">
              {QUICK_ACTIONS.map((action, i) => (
                <button className="quick-btn" key={i}>{action}</button>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
