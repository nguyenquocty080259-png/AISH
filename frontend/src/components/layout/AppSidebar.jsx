import { NavLink, Link } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import logo from "../../assets/images/hivemind-logo.png";

const ICONS = {
  home: <path d="M3 9.5 12 3l9 6.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1V9.5Z" />,
  doc: (<><path d="M6 2h8l4 4v16H6z" /><path d="M14 2v4h4" /></>),
  users: (<><circle cx="9" cy="8" r="3" /><path d="M3 21v-1a5 5 0 0 1 5-5h2a5 5 0 0 1 5 5v1" /><path d="M16 6a3 3 0 0 1 0 6" /><path d="M21 21v-1a5 5 0 0 0-3-4.6" /></>),
  collection: <path d="M4 4h16v14l-8-4-8 4z" />,
  heart: <path d="M12 21s-7-4.5-9.5-9A5 5 0 0 1 12 6a5 5 0 0 1 9.5 6c-2.5 4.5-9.5 9-9.5 9Z" />,
  ai: (<><path d="M12 3v3M12 18v3M3 12h3M18 12h3" /><circle cx="12" cy="12" r="4" /></>),
  report: <path d="M4 20V10M10 20V4M16 20v-6M2 20h20" />,
  trash: (<><path d="M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14" /></>),
  plus: <path d="M12 5v14M5 12h14" />,
};

function Icon({ name }) {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      {ICONS[name]}
    </svg>
  );
}

const MAIN = [
  { to: ROUTES.DASHBOARD, label: "Trang chủ", icon: "home" },
  { to: ROUTES.DOCUMENTS, label: "Tài liệu của tôi", icon: "doc" },
  { to: ROUTES.COMMUNITY, label: "Cộng đồng", icon: "users" },
  { to: ROUTES.SPACES, label: "Bộ sưu tập", icon: "collection" },
  { to: ROUTES.FAVORITES, label: "Yêu thích", icon: "heart" },
  { to: ROUTES.AI_CHAT, label: "AI HiveMind", icon: "ai" },
];
const SECONDARY = [
  { to: ROUTES.MY_REPORTS, label: "Báo cáo của tôi", icon: "report" },
  { to: ROUTES.TRASH, label: "Thùng rác", icon: "trash" },
];

function linkClass({ isActive }) {
  return [
    "flex items-center gap-3 rounded-input px-3 py-2.5 text-sm font-medium transition-colors justify-center lg:justify-start",
    isActive ? "bg-surface-soft text-primary-dark" : "text-secondary hover:bg-surface-soft hover:text-primary",
  ].join(" ");
}

export default function AppSidebar() {
  return (
    <aside className="sticky top-0 flex h-screen w-16 lg:w-64 flex-shrink-0 flex-col overflow-y-auto border-r border-border bg-surface px-2 lg:px-4 py-6">
      <Link to={ROUTES.HOME} className="flex items-center gap-2 px-1 lg:px-2 justify-center lg:justify-start">
        <img src={logo} alt="HiveMind" className="h-9 w-9 object-contain" />
        <div className="hidden lg:block leading-tight">
          <span className="block text-lg font-bold text-primary tracking-tight">HiveMind</span>
          <span className="block text-[10px] font-semibold uppercase tracking-widest text-secondary">AI Study Hub</span>
        </div>
      </Link>

      <nav className="mt-8 flex flex-1 flex-col gap-1">
        {MAIN.map((it) => (
          <NavLink key={it.to} to={it.to} className={linkClass}>
            <Icon name={it.icon} />
            <span className="hidden lg:inline">{it.label}</span>
          </NavLink>
        ))}
        <div className="my-3 border-t border-border" />
        {SECONDARY.map((it) => (
          <NavLink key={it.to} to={it.to} className={linkClass}>
            <Icon name={it.icon} />
            <span className="hidden lg:inline">{it.label}</span>
          </NavLink>
        ))}
      </nav>

      <Link to={ROUTES.DOCUMENTS}
        className="mt-4 flex items-center justify-center gap-2 rounded-input bg-primary py-3 font-semibold text-white transition-colors hover:bg-primary-dark">
        <Icon name="plus" />
        <span className="hidden lg:inline">Thêm tài liệu</span>
      </Link>
    </aside>
  );
}
