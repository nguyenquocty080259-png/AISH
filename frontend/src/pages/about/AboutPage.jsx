import imgT from "../../assets/images/T.png";
import imgN from "../../assets/images/N.png";
import imgA from "../../assets/images/A.png";
import imgL from "../../assets/images/L.png";

const VALUES = [
  { icon: "💡", title: "Sáng tạo", desc: "Ứng dụng AI đột phá để thay đổi cách sinh viên tiếp cận và xử lý khối lượng kiến thức khổng lồ." },
  { icon: "🤝", title: "Kết nối", desc: "Tạo dựng một hệ sinh thái học tập chung, nơi tri thức được chia sẻ và cộng hưởng không giới hạn." },
  { icon: "⚡", title: "Hiệu quả", desc: "Tiết kiệm thời gian đọc hiểu và tổng hợp, giúp người dùng tập trung vào việc tư duy và sáng tạo." },
];

const TEAM = [
  { name: "Nguyễn Quốc Tỷ", img: imgT },
  { name: "Nguyễn Thị Anh Như", img: imgN },
  { name: "Võ Minh Anh", img: imgA },
  { name: "Trần Vũ Đinh Lăng", img: imgL },
];

export default function AboutPage() {
  return (
    <div className="text-app">
      <section className="max-w-7xl mx-auto px-6 pt-16 pb-8 text-center">
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight">Về HiveMind</h1>
        <p className="text-secondary text-lg max-w-2xl mx-auto mt-4">Dự án đồ án môn SWP391, xây dựng nền tảng học tập kết hợp trí tuệ nhân tạo (AI) giúp tối ưu hóa việc quản lý và học hỏi từ tài liệu.</p>
      </section>

      <section className="max-w-7xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {VALUES.map((v) => (
            <div key={v.title} className="bg-surface p-8 rounded-card border border-border shadow-sm text-center">
              <div className="w-14 h-14 bg-surface-soft rounded-2xl flex items-center justify-center text-2xl mx-auto mb-5">{v.icon}</div>
              <h3 className="text-lg font-semibold mb-3">{v.title}</h3>
              <p className="text-secondary text-sm leading-relaxed">{v.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-surface-soft py-16">
        <div className="max-w-7xl mx-auto px-6 text-center">
          <h2 className="text-3xl font-bold">Đội ngũ phát triển</h2>
          <p className="text-secondary mt-3 mb-12">Những con người tâm huyết đằng sau dự án HiveMind.</p>
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
          <h2 className="text-2xl font-bold mb-4">Sứ mệnh của chúng tôi</h2>
          <p className="text-secondary text-lg leading-relaxed italic">HiveMind ra đời từ mong muốn đơn giản hóa việc học tập trong kỷ nguyên thông tin. Chúng tôi tin rằng với sự trợ giúp của AI, mỗi sinh viên đều có thể xây dựng một “tổ ong” tri thức thông minh, nơi mọi tài liệu đều được tổ chức khoa học và dễ dàng khai thác nhất.</p>
        </div>
      </section>
    </div>
  );
}
