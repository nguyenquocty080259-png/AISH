import { useEffect, useState } from "react";
import { Link, NavLink, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import LanguageSwitcher from "../common/LanguageSwitcher";
import logo from "../../assets/images/hivemind-logo.png";
import "./guest-chrome.css";

const LINKS = [
  { to: ROUTES.HOME, key: "nav.home", end: true },
  { to: ROUTES.FEATURES, key: "nav.features" },
  { to: ROUTES.HOW_IT_WORKS, key: "nav.howItWorks" },
  { to: ROUTES.ABOUT, key: "nav.about" },
];

function linkClass({ isActive }) {
  return `guest-nav__link has-custom-focus ${isActive ? "guest-nav__link--active" : ""}`.trim();
}

export default function GuestNavbar() {
  const { t } = useTranslation();
  const { pathname } = useLocation();
  const [open, setOpen] = useState(false);

  // Đổi trang thì đóng menu mobile.
  useEffect(() => { setOpen(false); }, [pathname]);

  return (
    <header className="guest-nav">
      <nav className="guest-nav__inner">
        <Link to={ROUTES.HOME} className="guest-nav__brand has-custom-focus">
          <img src={logo} alt="" className="guest-nav__logo" />
          <span className="guest-nav__brand-name">HiveMind</span>
        </Link>

        <div className="guest-nav__links">
          {LINKS.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end} className={linkClass}>
              {t(l.key)}
            </NavLink>
          ))}
        </div>

        <div className="guest-nav__actions">
          <LanguageSwitcher />
          <Link to={ROUTES.LOGIN} className="guest-nav__login has-custom-focus">
            {t("auth.login.submit")}
          </Link>
          <Link to={ROUTES.SIGNUP} className="guest-nav__cta has-custom-focus">
            {t("auth.signup.submit")}
          </Link>
        </div>

        <button
          type="button"
          className="guest-nav__toggle has-custom-focus"
          onClick={() => setOpen((o) => !o)}
          aria-expanded={open}
          aria-label={t("common.openMenu")}
        >
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            {open ? <path d="M18 6 6 18M6 6l12 12" /> : <path d="M3 6h18M3 12h18M3 18h18" />}
          </svg>
        </button>
      </nav>

      {open && (
        <div className="guest-nav__mobile">
          {LINKS.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end} className={linkClass}>
              {t(l.key)}
            </NavLink>
          ))}
          <div className="guest-nav__mobile-actions">
            <LanguageSwitcher />
            <Link to={ROUTES.LOGIN} className="guest-nav__login has-custom-focus">
              {t("auth.login.submit")}
            </Link>
            <Link to={ROUTES.SIGNUP} className="guest-nav__cta has-custom-focus">
              {t("auth.signup.submit")}
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
