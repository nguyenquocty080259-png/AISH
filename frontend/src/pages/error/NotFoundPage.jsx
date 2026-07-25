import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import "./notfound.css";

export default function NotFoundPage() {
  const { t } = useTranslation();
  return (
    <div className="notfound">
      <p className="notfound__code">404</p>
      <h1 className="notfound__title">{t("error.notFoundTitle")}</h1>
      <p className="notfound__desc">
        {t("error.notFoundDesc")}
      </p>
      <Link to={ROUTES.HOME} className="notfound__link">
        {t("error.backHome")}
      </Link>
    </div>
  );
}
