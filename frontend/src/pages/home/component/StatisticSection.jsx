import "./StatisticSection.css";

const STATS = [
  {
    value: "12K+",
    label: "Documents"
  },
  {
    value: "8K+",
    label: "Students"
  },
  {
    value: "120K+",
    label: "AI Questions"
  },
  {
    value: "96%",
    label: "Satisfaction"
  }
];

function StatisticSection() {
  return (
    <section id="statistics" className="statistics">

      {STATS.map((item) => (
        <div
          key={item.label}
          className="stat-card"
        >
          <h3>{item.value}</h3>

          <p>{item.label}</p>
        </div>
      ))}

    </section>
  );
}

export default StatisticSection;