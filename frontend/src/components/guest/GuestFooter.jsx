import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import logo from "../../assets/images/hivemind-logo.png";
import "./guest-chrome.css";

export default function GuestFooter() {
  const { t } = useTranslation();
  return (
    <footer className="guest-footer">
      <div className="guest-footer__inner">
        <Link to={ROUTES.HOME} className="guest-footer__brand has-custom-focus">
          <img src={logo} alt="" className="guest-footer__logo" />
          <span className="guest-footer__brand-name">HiveMind</span>
        </Link>

        <nav className="guest-footer__links" aria-label={t("common.footer.navAria")}>
          <Link to={ROUTES.FEATURES}>{t("nav.features")}</Link>
          <Link to={ROUTES.HOW_IT_WORKS}>{t("nav.howItWorks")}</Link>
          <Link to={ROUTES.ABOUT}>{t("nav.about")}</Link>
          <Link to={ROUTES.LOGIN}>{t("auth.login.submit")}</Link>
        </nav>

        <p className="guest-footer__copyright">{t("common.footer.copyright")}</p>
      </div>
    </footer>
  );
}
