import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../hooks/useAuth";
import { ROUTES } from "../../constants/routes";
import heroImg from "../../assets/images/hero-honeycomb.png";

const FEATURE_ICONS = ["📚", "🤖", "👥"];
const STEP_META = [
  { n: 1, filled: false },
  { n: 2, filled: false },
  { n: 3, filled: true },
];

export default function HomePage() {
  const { t } = useTranslation();
  const { isAuthenticated } = useAuth();
  const features = t("guest.home.features", { returnObjects: true });
  const steps = t("guest.home.steps", { returnObjects: true });
  return (
    <div className="text-app">
      <section className="max-w-7xl mx-auto px-6 pt-16 pb-24 flex flex-col md:flex-row items-center gap-12">
        <div className="w-full md:w-1/2 space-y-6">
          <span className="inline-flex items-center gap-2 bg-surface-soft text-primary-dark border border-primary rounded-pill px-4 py-1.5 text-xs font-semibold uppercase tracking-wider">{t("guest.home.badge")}</span>
          <h1 className="text-4xl md:text-5xl font-bold leading-tight tracking-tight">{t("guest.home.heroTitle")}</h1>
          <p className="text-secondary text-lg max-w-xl">{t("guest.home.heroSubtitle")}</p>
          <div className="flex flex-col sm:flex-row gap-4 pt-2">
            <Link to={isAuthenticated ? ROUTES.DASHBOARD : ROUTES.SIGNUP} className="bg-primary hover:bg-primary-dark text-white px-8 py-4 rounded-input font-bold text-center transition-colors">
              {isAuthenticated ? t("guest.home.ctaDashboard") : t("guest.home.ctaStart")}
            </Link>
            <Link to={ROUTES.FEATURES} className="bg-surface border border-border hover:border-primary text-app px-8 py-4 rounded-input font-semibold text-center transition-colors">{t("guest.home.explore")}</Link>
          </div>
        </div>
        <div className="w-full md:w-1/2">
          <div className="relative max-w-[480px] mx-auto">
            <div className="absolute inset-0 bg-surface-soft rounded-card scale-105 -z-10"></div>
            <img src={heroImg} alt="HiveMind" className="w-full rounded-card border border-border shadow-lg" />
          </div>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="flex justify-between items-end mb-10">
          <div>
            <h2 className="text-3xl font-bold">{t("guest.home.featuresTitle")}</h2>
            <p className="text-secondary mt-2">{t("guest.home.featuresSubtitle")}</p>
          </div>
          <Link to={ROUTES.FEATURES} className="text-primary text-sm font-semibold uppercase tracking-widest hover:text-primary-dark whitespace-nowrap">{t("guest.home.viewMore")}</Link>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {features.map((f, i) => (
            <div key={f.title} className="bg-surface p-8 rounded-card border border-border shadow-sm transition-transform hover:-translate-y-2">
              <div className="w-14 h-14 bg-surface-soft rounded-2xl flex items-center justify-center text-2xl mb-6">{FEATURE_ICONS[i]}</div>
              <h3 className="text-lg font-semibold mb-3">{f.title}</h3>
              <p className="text-secondary text-sm leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-surface-soft py-20">
        <div className="max-w-7xl mx-auto px-6 text-center">
          <div className="max-w-2xl mx-auto mb-14">
            <h2 className="text-3xl font-bold">{t("guest.home.stepsTitle")}</h2>
            <p className="text-secondary mt-3">{t("guest.home.stepsSubtitle")}</p>
          </div>
          <div className="flex flex-col md:flex-row justify-between items-start gap-12">
            {steps.map((s, i) => (
              <div key={STEP_META[i].n} className="flex-1 flex flex-col items-center">
                <div className={(STEP_META[i].filled ? "bg-primary text-white " : "bg-surface text-primary ") + "w-16 h-16 border-2 border-primary rounded-full flex items-center justify-center font-bold text-xl mb-6 shadow-sm"}>{STEP_META[i].n}</div>
                <h4 className="text-lg font-semibold mb-2">{s.title}</h4>
                <p className="text-secondary text-sm max-w-[240px]">{s.desc}</p>
              </div>
            ))}
          </div>
          <div className="mt-14">
            <Link to={ROUTES.HOW_IT_WORKS} className="text-primary font-semibold hover:text-primary-dark">{t("guest.home.stepsDetail")}</Link>
          </div>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="bg-surface-soft rounded-card p-8 md:p-16 flex flex-col md:flex-row items-center justify-between gap-8 text-center md:text-left">
          <div>
            <h2 className="text-3xl md:text-4xl font-bold text-primary-dark">{t("guest.home.ctaTitle")}</h2>
            <p className="text-secondary text-lg mt-3">{t("guest.home.ctaSubtitle")}</p>
          </div>
          <Link to={isAuthenticated ? ROUTES.DASHBOARD : ROUTES.SIGNUP} className="bg-primary hover:bg-primary-dark text-white px-10 py-5 rounded-pill font-bold whitespace-nowrap transition-colors">
            {isAuthenticated ? t("guest.home.ctaDashboard") : t("guest.home.ctaSignup")}
          </Link>
        </div>
      </section>
    </div>
  );
}
