import { useState } from "react";
import { useAuth } from "./AuthContext";
import "./dashboard.css";

// ============================================================
// Nav theo từng role — thêm role mới chỉ cần thêm key vào đây
// ============================================================

const NAV_BY_ROLE = {
  student: [
    { group: "TỔNG QUAN", items: [
      { icon: "🏠", label: "Dashboard",         id: "dashboard" },
      { icon: "📊", label: "Tiến độ học",       id: "progress"  },
    ]},
    { group: "HỌC TẬP", items: [
      { icon: "📁", label: "Tài liệu của tôi",  id: "my-docs"              },
      { icon: "🔖", label: "Đã lưu",            id: "saved"                },
      { icon: "🃏", label: "Flashcard",         id: "flashcard", badge: 12 },
    ]},
    { group: "AI", items: [
      { icon: "🤖", label: "AI Chatbot",        id: "chatbot"   },
      { icon: "📝", label: "Tóm tắt tài liệu",  id: "summarize" },
    ]},
    { group: "TÀI KHOẢN", items: [
      { icon: "👤", label: "Hồ sơ",             id: "profile"  },
      { icon: "⚙️",  label: "Cài đặt",          id: "settings" },
    ]},
  ],

  admin: [
    { group: "TỔNG QUAN", items: [
      { icon: "🏠", label: "Dashboard",         id: "dashboard" },
      { icon: "📊", label: "Thống kê",          id: "stats"     },
    ]},
    { group: "QUẢN LÝ", items: [
      { icon: "👥", label: "Sinh viên",         id: "users",  badge: 3 },
      { icon: "📄", label: "Tài liệu",          id: "docs"              },
      { icon: "✅", label: "Duyệt tài liệu",    id: "review", badge: 3 },
    ]},
    { group: "HỆ THỐNG", items: [
      { icon: "🔒", label: "Phân quyền",        id: "roles"    },
      { icon: "⚙️",  label: "Cài đặt",          id: "settings" },
    ]},
  ],
};

// Tên viết tắt avatar từ tên user
function getInitials(name = "") {
  return name.split(" ").slice(-2).map(w => w[0]).join("").toUpperCase() || "?";
}

// ============================================================

export default function DashboardLayout({ children }) {
  const { user, role, logout } = useAuth();
  const [activeNav, setActiveNav] = useState("dashboard");

  const nav = NAV_BY_ROLE[role] ?? [];

  return (
    <div className="db-root">

      {/* Topbar */}
      <header className="topbar">
        <div className="topbar-logo">
          <div className="logo-icon">A</div>
          <span className="logo-name">AI Study Hub</span>
        </div>
        <div className="topbar-right">
          <div className="search-wrap">
            <span>🔍</span>
            <input placeholder="Tìm tài liệu, môn học..." aria-label="Tìm kiếm" />
          </div>
          <button className="tb-btn" aria-label="Thông báo">🔔</button>
          <div className="tb-user" onClick={logout} title="Đăng xuất" aria-label="Đăng xuất">
            <div className="tb-avatar">{getInitials(user?.name)}</div>
            <div className="tb-user-info">
              <span className="tb-user-name">{user?.name ?? "..."}</span>
              <span className="tb-user-role">{role === "admin" ? "Quản trị viên" : "Sinh viên"}</span>
            </div>
          </div>
        </div>
      </header>

      {/* Sidebar */}
      <aside className="sidebar" aria-label="Điều hướng">
        {nav.map(group => (
          <div key={group.group}>
            <div className="nav-group-label">{group.group}</div>
            {group.items.map(item => (
              <div
                key={item.id}
                className={`nav-item${activeNav === item.id ? " active" : ""}`}
                onClick={() => setActiveNav(item.id)}
                role="button"
                tabIndex={0}
                aria-current={activeNav === item.id ? "page" : undefined}
              >
                <span className="nav-icon" aria-hidden="true">{item.icon}</span>
                {item.label}
                {item.badge && <span className="nav-badge">{item.badge}</span>}
              </div>
            ))}
          </div>
        ))}

        {/* Logout ở cuối sidebar */}
        <div style={{ marginTop: "auto", paddingTop: 16 }}>
          <div className="nav-item nav-item--logout" onClick={logout} role="button" tabIndex={0}>
            <span className="nav-icon" aria-hidden="true">🚪</span>
            Đăng xuất
          </div>
        </div>
      </aside>

      {/* Nội dung chính — render StudentDashboard hoặc AdminDashboard */}
      <main className="main-scroll">
        {children}
      </main>

    </div>
  );
}
