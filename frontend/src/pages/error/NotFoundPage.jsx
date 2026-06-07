import { useNavigate } from "react-router-dom";
import "./NotFoundPage.css";

// =============================================
// DATA — chỉnh text và links tuỳ ý
// =============================================

const CONTENT = {
  title: "Không tìm thấy trang",
  sub: "Trang bạn đang tìm không tồn tại, đã bị xóa hoặc đường dẫn không chính xác.",
  btnHome: "Về trang chủ",
  btnBack: "Quay lại",
  hint: "Hoặc kiểm tra lại đường dẫn URL",
};

const QUICK_LINKS = [
  { label: "Tìm tài liệu", href: "/#popularDocumentSection" },
  { label: "AI Chatbot",   href: "/#aiShowcaseSection"       },
  { label: "Dashboard",   href: "/dashboard"                 },
  { label: "Đăng nhập",   href: "/login"                     },
];

// =============================================

export default function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <div className="nf-root">

      <div className="nf-code" aria-label="404">
        <span className="nf-digit">4</span>
        <span className="nf-zero">
          <span className="nf-zero__text">0</span>
          <span className="nf-zero__icon" aria-hidden="true">🔍</span>
        </span>
        <span className="nf-digit">4</span>
      </div>

      <div className="nf-divider" />

      <h1 className="nf-title">{CONTENT.title}</h1>
      <p className="nf-sub">{CONTENT.sub}</p>

      <div className="nf-actions">
        <button className="btn-home" onClick={() => navigate("/")}>
          🏠 {CONTENT.btnHome}
        </button>
        <button className="btn-back" onClick={() => navigate(-1)}>
          ← {CONTENT.btnBack}
        </button>
      </div>

      <nav className="nf-links" aria-label="Trang gợi ý">
        {QUICK_LINKS.map(({ label, href }) => (
          <a key={label} className="nf-link" href={href}>{label}</a>
        ))}
      </nav>

      <p className="nf-hint">{CONTENT.hint}</p>

    </div>
  );
}