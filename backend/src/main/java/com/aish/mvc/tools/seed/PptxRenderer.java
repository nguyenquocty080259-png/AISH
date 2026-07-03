package com.aish.mvc.tools.seed;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

// 1 slide tiêu đề + 1 slide/đoạn văn — mô phỏng slide bài giảng thật (không có khái niệm
// "trang" như PDF, nên ingest phía backend sẽ lưu page=null cho các chunk từ file này.
final class PptxRenderer {

    private PptxRenderer() {
    }

    static void render(File outputFile, String title, List<String> paragraphs) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            Dimension pageSize = ppt.getPageSize();

            XSLFSlide titleSlide = ppt.createSlide();
            XSLFTextBox titleBox = titleSlide.createTextBox();
            titleBox.setAnchor(new Rectangle(50, 80, pageSize.width - 100, 160));
            XSLFTextParagraph titlePar = titleBox.addNewTextParagraph();
            XSLFTextRun titleRun = titlePar.addNewTextRun();
            titleRun.setText(title);
            titleRun.setFontSize(32.0);
            titleRun.setBold(true);

            for (String paragraph : paragraphs) {
                XSLFSlide slide = ppt.createSlide();
                XSLFTextBox box = slide.createTextBox();
                box.setAnchor(new Rectangle(50, 50, pageSize.width - 100, pageSize.height - 100));
                XSLFTextParagraph par = box.addNewTextParagraph();
                XSLFTextRun run = par.addNewTextRun();
                run.setText(paragraph);
                run.setFontSize(20.0);
            }

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                ppt.write(out);
            }
        }
    }
}
