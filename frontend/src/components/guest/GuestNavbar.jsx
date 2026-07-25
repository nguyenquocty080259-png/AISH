import { Link, NavLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import LanguageSwitcher from "../common/LanguageSwitcher";
import logo from "../../assets/images/hivemind-logo.png";

const LINKS = [
  { to: ROUTES.HOME, key: "nav.home", end: true },
  { to: ROUTES.FEATURES, key: "nav.features" },
  { to: ROUTES.HOW_IT_WORKS, key: "nav.howItWorks" },
  { to: ROUTES.ABOUT, key: "nav.about" },
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
  const { t } = useTranslation();
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
              {t(l.key)}
            </NavLink>
          ))}
        </div>
        <div className="flex items-center gap-3">
          <LanguageSwitcher />
          <Link to={ROUTES.LOGIN} className="text-sm text-secondary hover:text-primary px-4 py-2 transition-colors">{t("auth.login.submit")}</Link>
          <Link to={ROUTES.SIGNUP} className="bg-primary hover:bg-primary-dark text-white text-sm font-semibold px-6 py-2.5 rounded-pill transition-colors">{t("auth.signup.submit")}</Link>
        </div>
      </nav>
    </header>
  );
}
