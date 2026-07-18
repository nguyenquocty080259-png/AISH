package com.aish.mvc.service.doc;

import com.aish.mvc.entity.doc.DocFile;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * extract() (A3/T4) không bao giờ được ném lỗi ra ngoài — caller (DocumentController) coi
 * null là "không trích được" -> 204. 3 điều cần xác nhận: DOCX ra text thật, PPTX nối slide
 * bằng dòng phân cách "— Slide N —" đúng thứ tự, và file hỏng trả về null thay vì exception.
 */
class DocumentTextExtractorTest {

    private final DocumentTextExtractor extractor = new DocumentTextExtractor();

    private static Resource docxResource(String... paragraphs) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            for (String p : paragraphs) {
                XWPFParagraph par = document.createParagraph();
                XWPFRun run = par.createRun();
                run.setText(p);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return new ByteArrayResource(out.toByteArray());
        }
    }

    private static Resource pptxResource(String... slideTexts) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            for (String text : slideTexts) {
                XSLFSlide slide = ppt.createSlide();
                XSLFTextBox box = slide.createTextBox();
                box.setAnchor(new java.awt.Rectangle(50, 50, 400, 300));
                box.addNewTextParagraph().addNewTextRun().setText(text);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ppt.write(out);
            return new ByteArrayResource(out.toByteArray());
        }
    }

    @Test
    void extractsTextFromDocx() throws Exception {
        Resource resource = docxResource("Chương 1: Giới thiệu", "Nội dung bài học.");
        DocFile docFile = DocFile.builder()
                .fileName("bai-giang.docx")
                .fileType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .build();

        String text = extractor.extract(resource, docFile);

        assertTrue(text.contains("Chương 1: Giới thiệu"));
        assertTrue(text.contains("Nội dung bài học."));
    }

    @Test
    void joinsSlidesWithSeparatorInOrder() throws Exception {
        Resource resource = pptxResource("Slide đầu tiên", "Slide thứ hai", "Slide thứ ba");
        DocFile docFile = DocFile.builder()
                .fileName("bai-giang.pptx")
                .fileType("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                .build();

        String text = extractor.extract(resource, docFile);

        int idxSlide1 = text.indexOf("— Slide 1 —");
        int idxContent1 = text.indexOf("Slide đầu tiên");
        int idxSlide2 = text.indexOf("— Slide 2 —");
        int idxContent2 = text.indexOf("Slide thứ hai");
        int idxSlide3 = text.indexOf("— Slide 3 —");
        int idxContent3 = text.indexOf("Slide thứ ba");

        assertTrue(idxSlide1 >= 0 && idxSlide1 < idxContent1);
        assertTrue(idxContent1 < idxSlide2 && idxSlide2 < idxContent2);
        assertTrue(idxContent2 < idxSlide3 && idxSlide3 < idxContent3);
    }

    @Test
    void corruptDocxReturnsNullInsteadOfThrowing() {
        Resource resource = new ByteArrayResource("not a real docx file".getBytes(StandardCharsets.UTF_8));
        DocFile docFile = DocFile.builder().fileName("broken.docx").build();

        assertNull(extractor.extract(resource, docFile));
    }

    @Test
    void corruptPptxReturnsNullInsteadOfThrowing() {
        Resource resource = new ByteArrayResource("not a real pptx file".getBytes(StandardCharsets.UTF_8));
        DocFile docFile = DocFile.builder().fileName("broken.pptx").build();

        assertNull(extractor.extract(resource, docFile));
    }
}
