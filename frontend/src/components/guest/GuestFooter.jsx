import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import logo from "../../assets/images/hivemind-logo.png";

export default function GuestFooter() {
  const { t } = useTranslation();
  return (
    <footer className="bg-surface-soft border-t border-border">
      <div className="flex flex-col md:flex-row justify-between items-center w-full max-w-7xl mx-auto px-6 py-12 gap-6">
        <Link to={ROUTES.HOME} className="flex items-center gap-3">
          <img src={logo} alt="HiveMind" className="w-8 h-8 object-contain" />
          <span className="text-base font-bold text-primary">HiveMind</span>
        </Link>
        <div className="flex flex-wrap justify-center gap-8">
          <Link to={ROUTES.FEATURES} className="text-sm text-secondary hover:text-primary transition-colors">{t("nav.features")}</Link>
          <Link to={ROUTES.HOW_IT_WORKS} className="text-sm text-secondary hover:text-primary transition-colors">{t("nav.howItWorks")}</Link>
          <Link to={ROUTES.ABOUT} className="text-sm text-secondary hover:text-primary transition-colors">{t("nav.about")}</Link>
          <Link to={ROUTES.LOGIN} className="text-sm text-secondary hover:text-primary transition-colors">{t("auth.login.submit")}</Link>
        </div>
        <div className="text-sm text-secondary">{t("common.footer.copyright")}</div>
      </div>
    </footer>
  );
}
