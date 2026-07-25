import { useTranslation } from "react-i18next";
import imgT from "../../assets/images/T.png";
import imgN from "../../assets/images/N.png";
import imgA from "../../assets/images/A.png";
import imgL from "../../assets/images/L.png";

const VALUE_ICONS = ["💡", "🤝", "⚡"];

const TEAM = [
  { name: "Nguyễn Quốc Tỷ", img: imgT },
  { name: "Nguyễn Thị Anh Như", img: imgN },
  { name: "Võ Minh Anh", img: imgA },
  { name: "Trần Vũ Đinh Lăng", img: imgL },
];

export default function AboutPage() {
  const { t } = useTranslation();
  const values = t("guest.about.values", { returnObjects: true });
  return (
    <div className="text-app">
      <section className="max-w-7xl mx-auto px-6 pt-16 pb-8 text-center">
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight">{t("guest.about.title")}</h1>
        <p className="text-secondary text-lg max-w-2xl mx-auto mt-4">{t("guest.about.subtitle")}</p>
      </section>

      <section className="max-w-7xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {values.map((v, i) => (
            <div key={v.title} className="bg-surface p-8 rounded-card border border-border shadow-sm text-center">
              <div className="w-14 h-14 bg-surface-soft rounded-2xl flex items-center justify-center text-2xl mx-auto mb-5">{VALUE_ICONS[i]}</div>
              <h3 className="text-lg font-semibold mb-3">{v.title}</h3>
              <p className="text-secondary text-sm leading-relaxed">{v.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-surface-soft py-16">
        <div className="max-w-7xl mx-auto px-6 text-center">
          <h2 className="text-3xl font-bold">{t("guest.about.teamTitle")}</h2>
          <p className="text-secondary mt-3 mb-12">{t("guest.about.teamSubtitle")}</p>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            {TEAM.map((m) => (
              <div key={m.name} className="flex flex-col items-center">
                <img src={m.img} alt={m.name} className="w-32 h-32 rounded-full object-cover border-4 border-white shadow-md mb-4" />
                <p className="font-semibold">{m.name}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-6 py-16">
        <div className="bg-surface-soft rounded-card p-8 md:p-14 text-center max-w-3xl mx-auto">
          <div className="text-5xl text-primary leading-none mb-4">“</div>
          <h2 className="text-2xl font-bold mb-4">{t("guest.about.missionTitle")}</h2>
          <p className="text-secondary text-lg leading-relaxed italic">{t("guest.about.missionText")}</p>
        </div>
      </section>
    </div>
  );
}
