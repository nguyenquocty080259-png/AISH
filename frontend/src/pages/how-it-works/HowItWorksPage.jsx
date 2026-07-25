import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES } from "../../constants/routes";
import img1 from "../../assets/images/how-1-upload.png";
import img2 from "../../assets/images/how-2-categorize.png";
import img3 from "../../assets/images/how-3-collaborate.png";
import img4 from "../../assets/images/how-4-ai-chat.png";

const STEP_META = [
  { n: 1, icon: "📤", image: img1 },
  { n: 2, icon: "📚", image: img2 },
  { n: 3, icon: "👥", image: img3 },
  { n: 4, icon: "🤖", image: img4 },
];

export default function HowItWorksPage() {
  const { t } = useTranslation();
  const steps = t("guest.howItWorks.steps", { returnObjects: true });
  return (
    <div className="text-app">
      <section className="max-w-7xl mx-auto px-6 pt-16 pb-8 text-center">
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight">{t("guest.howItWorks.title")}</h1>
        <p className="text-secondary text-lg max-w-2xl mx-auto mt-4">{t("guest.howItWorks.subtitle")}</p>
      </section>
      <div className="max-w-7xl mx-auto px-6 py-12 space-y-16">
        {steps.map((s, i) => (
          <div key={STEP_META[i].n} className={"flex flex-col gap-8 items-center md:flex-row" + (i % 2 ? " md:flex-row-reverse" : "")}>
            <div className="w-full md:w-1/2">
              <div className="flex items-start gap-5">
                <div className="shrink-0 w-14 h-14 bg-primary text-white rounded-full flex items-center justify-center font-bold text-xl shadow-sm">{STEP_META[i].n}</div>
                <div>
                  <div className="text-3xl mb-2">{STEP_META[i].icon}</div>
                  <h3 className="text-xl font-bold mb-2">{s.title}</h3>
                  <p className="text-secondary">{s.desc}</p>
                </div>
              </div>
            </div>
            <div className="w-full md:w-1/2">
              <img src={STEP_META[i].image} alt={s.title} className="w-full rounded-card border border-border shadow-md" />
            </div>
          </div>
        ))}
      </div>
      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="bg-surface-soft rounded-card p-8 md:p-14 text-center">
          <h2 className="text-3xl font-bold text-primary-dark">{t("guest.howItWorks.ctaTitle")}</h2>
          <p className="text-secondary text-lg mt-3 mb-6">{t("guest.howItWorks.ctaSubtitle")}</p>
          <Link to={ROUTES.SIGNUP} className="inline-block bg-primary hover:bg-primary-dark text-white px-8 py-4 rounded-pill font-bold transition-colors">{t("guest.howItWorks.ctaSignup")}</Link>
        </div>
      </section>
    </div>
  );
}
