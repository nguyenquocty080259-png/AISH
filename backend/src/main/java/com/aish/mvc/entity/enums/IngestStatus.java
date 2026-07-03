package com.aish.mvc.entity.enums;

// Trạng thái AI-ingest (chunk + embed) của tài liệu — cho phép FE biết có nên hiển thị
// tính năng "hỏi AI" hay không mà không cần gọi lại /api/ai/ingest.
public enum IngestStatus {
    NOT_INGESTED,       // Chưa từng ingest thành công (mặc định)
    INGESTED,           // Đã chunk + embed xong, có thể hỏi AI
    UNSUPPORTED_FORMAT  // Định dạng file không hỗ trợ đọc AI (ảnh, video, zip, xlsx...)
}
