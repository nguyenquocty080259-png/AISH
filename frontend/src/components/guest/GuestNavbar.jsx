import { Link, NavLink } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import logo from "../../assets/images/hivemind-logo.png";

const LINKS = [
  { to: ROUTES.HOME, label: "Trang chủ", end: true },
  { to: ROUTES.FEATURES, label: "Tính năng" },
  { to: ROUTES.HOW_IT_WORKS, label: "Cách hoạt động" },
  { to: ROUTES.ABOUT, label: "Giới thiệu" },
];

function linkClass({ isActive }) {
  return [
    "text-sm transition-colors pb-1",
    isActive
      ? "text-primary font-bold border-b-2 border-primary"
      : "text-secondary hover:text-primary",
  ].join(" ");
}

export default function GuestNavbar() {
  return (
    <header className="sticky top-0 z-50 bg-surface border-b border-border">
      <nav className="flex items-center justify-between w-full max-w-7xl mx-auto px-6 py-4">
        <Link to={ROUTES.HOME} className="flex items-center gap-3">
          <img src={logo} alt="HiveMind" className="w-9 h-9 object-contain" />
          <span className="text-lg font-bold text-primary tracking-tight">HiveMind</span>
        </Link>
        <div className="hidden md:flex items-center gap-8">
          {LINKS.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end} className={linkClass}>
              {l.label}
            </NavLink>
          ))}
        </div>
        <div className="flex items-center gap-3">
          <Link to={ROUTES.LOGIN} className="text-sm text-secondary hover:text-primary px-4 py-2 transition-colors">Đăng nhập</Link>
          <Link to={ROUTES.SIGNUP} className="bg-primary hover:bg-primary-dark text-white text-sm font-semibold px-6 py-2.5 rounded-pill transition-colors">Đăng ký</Link>
        </div>
      </nav>
    </header>
  );
}
