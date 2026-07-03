package com.aish.mvc.tools.seed;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sinh PDF nhiều trang, có wrap dòng + phân trang thủ công (PDFBox không có flowable text
 * API sẵn). Nhúng font TrueType Unicode thật (không dùng font 14-chuẩn) vì nội dung tiếng
 * Việt có dấu — base-14 fonts của PDF không hỗ trợ đủ glyph tiếng Việt.
 */
final class PdfTextRenderer {

    // Margin/font/leading cố tình generous (không phải "12pt Times chuẩn") để 1 bài học ~250-300
    // từ đủ tràn sang trang 2 — bắt buộc để test citation có page thật một cách nghiêm túc, thay
    // vì mọi PDF chỉ vỏn vẹn 1 trang. Đây là lựa chọn trình bày hợp lý (font to, giãn dòng rộng
    // vẫn là cách trình bày tài liệu thật), không phải nhồi nội dung giả để né việc phải multi-page.
    private static final float MARGIN = 130f;
    private static final float TITLE_SIZE = 22f;
    private static final float BODY_SIZE = 18f;
    private static final float LEADING = 34f;

    private record FontPaths(String regular, String bold) {
    }

    // Thử theo thứ tự; máy Windows luôn có Arial nên candidate đầu là đường thường gặp nhất.
    private static final List<FontPaths> CANDIDATES = List.of(
            new FontPaths("C:/Windows/Fonts/arial.ttf", "C:/Windows/Fonts/arialbd.ttf"),
            new FontPaths("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"),
            new FontPaths("/System/Library/Fonts/Supplemental/Arial.ttf",
                    "/System/Library/Fonts/Supplemental/Arial Bold.ttf")
    );

    private PdfTextRenderer() {
    }

    static void render(File outputFile, String title, List<String> paragraphs) throws IOException {
        FontPaths fontPaths = resolveFontPaths();

        try (PDDocument document = new PDDocument()) {
            PDFont font = PDType0Font.load(document, new File(fontPaths.regular()));
            PDFont boldFont = PDType0Font.load(document, new File(fontPaths.bold()));

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(document, page);
            float maxWidth = PDRectangle.A4.getWidth() - 2 * MARGIN;
            float y = PDRectangle.A4.getHeight() - MARGIN;

            for (String line : wrapText(title, boldFont, TITLE_SIZE, maxWidth)) {
                cs.beginText();
                cs.setFont(boldFont, TITLE_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(line);
                cs.endText();
                y -= TITLE_SIZE + 6;
            }
            y -= 10;

            for (String paragraph : paragraphs) {
                for (String line : wrapText(paragraph, font, BODY_SIZE, maxWidth)) {
                    if (y < MARGIN + LEADING) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        cs = new PDPageContentStream(document, page);
                        y = PDRectangle.A4.getHeight() - MARGIN;
                    }
                    cs.beginText();
                    cs.setFont(font, BODY_SIZE);
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText(line);
                    cs.endText();
                    y -= LEADING;
                }
                y -= LEADING * 0.6f;
            }
            cs.close();

            document.save(outputFile);
        }
    }

    private static List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String rawLine : text.split("\n")) {
            StringBuilder current = new StringBuilder();
            for (String word : rawLine.split(" ")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                float width = font.getStringWidth(candidate) / 1000f * fontSize;
                if (width > maxWidth && !current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                }
                else {
                    current = new StringBuilder(candidate);
                }
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
        }
        return lines;
    }

    private static FontPaths resolveFontPaths() {
        String override = System.getProperty("app.seed.font");
        if (override != null && new File(override).isFile()) {
            String boldOverride = System.getProperty("app.seed.fontBold", override);
            return new FontPaths(override, boldOverride);
        }
        for (FontPaths candidate : CANDIDATES) {
            if (new File(candidate.regular()).isFile() && new File(candidate.bold()).isFile()) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Không tìm thấy font Unicode TTF để nhúng PDF tiếng Việt (đã thử: " + CANDIDATES
                        + "). Chỉ định qua -Dapp.seed.font=<đường-dẫn-font-thường.ttf> "
                        + "-Dapp.seed.fontBold=<đường-dẫn-font-đậm.ttf>.");
    }
}
