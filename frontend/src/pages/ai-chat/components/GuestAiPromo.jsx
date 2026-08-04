import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../../constants/routes";

// Trang quảng bá tính năng AI Chat cho KHÁCH chưa đăng nhập, mời đăng nhập/đăng ký để dùng thử.
export default function GuestAiPromo() {
  const { t } = useTranslation();
  return (
    <main className="guest-ai-promo">
      <section className="guest-ai-promo__card">
        <div className="guest-ai-promo__media" aria-label={t("aiChat.videoAria")}>
          <span className="guest-ai-promo__play" aria-hidden="true" />
          <p>{t("aiChat.videoSoon")}</p>
        </div>

        <div className="guest-ai-promo__content">
          <span className="guest-ai-promo__eyebrow">{t("aiChat.promoEyebrow")}</span>
          <h1>{t("aiChat.promoTitle")}</h1>
          <p className="guest-ai-promo__intro">
            {t("aiChat.promoIntro")}
          </p>

          <ul className="guest-ai-promo__features">
            <li>{t("aiChat.promoFeature1")}</li>
            <li>{t("aiChat.promoFeature2")}</li>
            <li>{t("aiChat.promoFeature3")}</li>
            <li>{t("aiChat.promoFeature4")}</li>
          </ul>

          <div className="guest-ai-promo__actions">
            <Link to={ROUTES.LOGIN} className="guest-ai-promo__button guest-ai-promo__button--primary">
              {t("aiChat.tryNow")}
            </Link>
            <Link to={ROUTES.SIGNUP} className="guest-ai-promo__button guest-ai-promo__button--secondary">
              {t("aiChat.signup")}
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
