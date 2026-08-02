package com.aish.mvc.service.doc;

import com.aish.mvc.entity.doc.DocFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trích xuất text best-effort cho GET /api/documents/{id}/preview-text (A3/T4) — dùng cho
 * PPTX (chưa có trình xem in-page riêng) và làm fallback khi DocxViewer (docx-preview, FE)
 * render lỗi. Không bao giờ ném lỗi ra ngoài: mọi lỗi đọc/parse bị nuốt và log warning, caller
 * coi kết quả null/rỗng là "không trích xuất được" để trả 204 (xem DocumentController).
 */
@Slf4j
@Service
public class DocumentTextExtractor {

    /**
     * Đọc chữ trong file ra dạng văn bản thuần.
     *
     * <p>Đầu vào: nội dung file (Resource) + bản ghi file (để biết tên và loại). Trả về: chuỗi
     * văn bản, hoặc null nếu không đọc được / file rỗng.
     *
     * <p>Chọn cách đọc theo loại file: DOCX dùng thư viện Word của Apache POI, PPTX ghép chữ
     * từng slide, các loại còn lại đọc bằng Tika.
     */
    public String extract(Resource resource, DocFile docFile) {
        String type = docFile.getFileType() != null ? docFile.getFileType().toLowerCase() : "";
        String name = docFile.getFileName() != null ? docFile.getFileName().toLowerCase() : "";

        try {
            if (type.contains("wordprocessingml") || name.endsWith(".docx")) {
                return extractDocx(resource);
            }
            if (type.contains("presentationml") || name.endsWith(".pptx")) {
                return extractPptx(resource);
            }
            return extractWithTika(resource);
        } catch (Exception e) {
            log.warn("Không trích xuất được text từ file '{}': {}", docFile.getFileName(), e.getMessage());
            return null;
        }
    }

    private String extractDocx(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream();
             XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return blankToNull(extractor.getText());
        }
    }

    // Nối các slide bằng dòng phân cách "— Slide N —" theo đúng thứ tự slide trong file.
    private String extractPptx(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream();
             XMLSlideShow slideShow = new XMLSlideShow(is)) {
            List<XSLFSlide> slides = slideShow.getSlides();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < slides.size(); i++) {
                sb.append("— Slide ").append(i + 1).append(" —\n");
                for (XSLFShape shape : slides.get(i).getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String text = textShape.getText();
                        if (text != null && !text.isBlank()) {
                            sb.append(text).append("\n");
                        }
                    }
                }
                sb.append("\n");
            }
            return blankToNull(sb.toString());
        }
    }

    private String extractWithTika(Resource resource) {
        List<Document> docs = new TikaDocumentReader(resource).get();
        String text = docs.stream()
                .map(Document::getText)
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.joining("\n\n"));
        return blankToNull(text);
    }

    private String blankToNull(String text) {
        return (text == null || text.isBlank()) ? null : text;
    }
}
