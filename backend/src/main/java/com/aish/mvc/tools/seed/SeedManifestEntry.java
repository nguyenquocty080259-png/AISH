package com.aish.mvc.tools.seed;

// 1 dòng manifest = 1 file đã sinh — đủ thông tin để bước seed-DB sau này map
// file -> DocDocument/DocFile/Subject mà không phải đoán lại từ tên file.
public record SeedManifestEntry(
        String storedFileName,   // tên file thật trên đĩa: {uuid}_{originalFileName} — khớp FileStorageServiceImpl
        String originalFileName, // tên "gốc" mô phỏng người dùng đã upload
        String fileType,         // MIME — khớp đúng check trong DocEmbeddingServiceImpl.resolveIngestFormat
        String format,           // PDF | DOCX | PPTX | TXT
        String level,            // THPT | UNIVERSITY
        String track,            // IT | IB | MKT (chỉ có ở UNIVERSITY, null với THPT)
        String subjectOrDomain,  // vd. "Toán", "AI/ML"
        String title,
        long fileSizeBytes
) {
}
