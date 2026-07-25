import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import imgLibrary from "../../assets/images/feature-library.png";
import imgCommunity from "../../assets/images/feature-community.png";

const BLOCK_META = [
  { icon: "📚", image: imgLibrary },
  { icon: "🤖", image: null },
  { icon: "👥", image: imgCommunity },
  { icon: "🗂️", image: null },
];

export default function FeaturesPage() {
  const { t } = useTranslation();
  const blocks = t("guest.features.blocks", { returnObjects: true });
  return (
    <div className="text-app">
      <section className="max-w-7xl mx-auto px-6 pt-16 pb-8 text-center">
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight">{t("guest.features.title")}</h1>
        <p className="text-secondary text-lg max-w-2xl mx-auto mt-4">{t("guest.features.subtitle")}</p>
      </section>
      <div className="max-w-7xl mx-auto px-6 py-12 space-y-20">
        {blocks.map((b, i) => (
          <div key={b.title} className={"flex flex-col gap-10 items-center md:flex-row" + (i % 2 ? " md:flex-row-reverse" : "")}>
            <div className="w-full md:w-1/2 space-y-5">
              <span className="inline-flex items-center gap-2 bg-surface-soft text-primary-dark rounded-pill px-4 py-1 text-sm font-semibold">{BLOCK_META[i].icon} {b.chip}</span>
              <h2 className="text-2xl md:text-3xl font-bold">{b.title}</h2>
              <p className="text-secondary">{b.desc}</p>
              <ul className="space-y-3">
                {b.bullets.map((bullet) => (
                  <li key={bullet} className="flex items-start gap-3"><span className="text-primary font-bold mt-0.5">✓</span><span>{bullet}</span></li>
                ))}
              </ul>
            </div>
            <div className="w-full md:w-1/2">
              {BLOCK_META[i].image ? (
                <img src={BLOCK_META[i].image} alt={b.title} className="w-full rounded-card border border-border shadow-md" />
              ) : (
                <div className="w-full aspect-video rounded-card bg-surface-soft border border-border flex items-center justify-center text-6xl">{BLOCK_META[i].icon}</div>
              )}
            </div>
          </div>
        ))}
      </div>
      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="bg-surface-soft rounded-card p-8 md:p-14 text-center">
          <h2 className="text-3xl font-bold text-primary-dark">{t("guest.features.ctaTitle")}</h2>
          <p className="text-secondary text-lg mt-3 mb-6">{t("guest.features.ctaSubtitle")}</p>
          <Link to={ROUTES.SIGNUP} className="inline-block bg-primary hover:bg-primary-dark text-white px-8 py-4 rounded-pill font-bold transition-colors">{t("guest.features.ctaSignup")}</Link>
        </div>
      </section>
    </div>
  );
}
