import { Link } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import logo from "../../assets/images/hivemind-logo.png";

export default function GuestFooter() {
  return (
    <footer className="bg-surface-soft border-t border-border">
      <div className="flex flex-col md:flex-row justify-between items-center w-full max-w-7xl mx-auto px-6 py-12 gap-6">
        <Link to={ROUTES.HOME} className="flex items-center gap-3">
          <img src={logo} alt="HiveMind" className="w-8 h-8 object-contain" />
          <span className="text-base font-bold text-primary">HiveMind</span>
        </Link>
        <div className="flex flex-wrap justify-center gap-8">
          <Link to={ROUTES.FEATURES} className="text-sm text-secondary hover:text-primary transition-colors">Tính năng</Link>
          <Link to={ROUTES.HOW_IT_WORKS} className="text-sm text-secondary hover:text-primary transition-colors">Cách hoạt động</Link>
          <Link to={ROUTES.ABOUT} className="text-sm text-secondary hover:text-primary transition-colors">Giới thiệu</Link>
          <Link to={ROUTES.LOGIN} className="text-sm text-secondary hover:text-primary transition-colors">Đăng nhập</Link>
        </div>
        <div className="text-sm text-secondary">© 2026 HiveMind</div>
      </div>
    </footer>
  );
}
