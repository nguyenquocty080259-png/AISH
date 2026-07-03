package com.aish.mvc.tools.seed;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

final class DocxRenderer {

    private DocxRenderer() {
    }

    static void render(File outputFile, String title, List<String> paragraphs) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph titlePar = document.createParagraph();
            XWPFRun titleRun = titlePar.createRun();
            titleRun.setText(title);
            titleRun.setBold(true);
            titleRun.setFontSize(18);

            for (String paragraph : paragraphs) {
                XWPFParagraph par = document.createParagraph();
                XWPFRun run = par.createRun();
                run.setText(paragraph);
                run.setFontSize(12);
            }

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }
        }
    }
}
