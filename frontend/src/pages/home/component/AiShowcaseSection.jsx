import "./AiShowcaseSection.css";

function AiShowcaseSection() {
  return (
    <section id="ai-showcase" className="ai-showcase">

      <div className="ai-showcase-left">
        <span className="ai-badge">
          ✦ AI Assistant
        </span>

        <h2 className="ai-title">
          Học cùng AI
          <br />
          mọi lúc mọi nơi
        </h2>

        <p className="ai-description">
          Đặt câu hỏi về tài liệu,
          nhận giải thích chi tiết,
          tóm tắt nội dung và hỗ trợ học tập tức thì.
        </p>

        <button className="ai-button">
          Hỏi AI ngay
        </button>
      </div>

      <div className="ai-chat-card">

        <div className="user-message">
          JWT là gì?
        </div>

        <div className="ai-message">
          JWT (JSON Web Token) là chuẩn dùng để xác thực
          và truyền dữ liệu an toàn giữa client và server.
        </div>

      </div>

    </section>
  );
}

export default AiShowcaseSection;