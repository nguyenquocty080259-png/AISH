package com.aish.mvc.tools.seed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.File;
import java.util.List;

final class TxtRenderer {

    private TxtRenderer() {
    }

    static void render(File outputFile, String title, List<String> paragraphs) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n\n");
        for (String paragraph : paragraphs) {
            sb.append(paragraph).append("\n\n");
        }
        Files.writeString(outputFile.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }
}
