package com.aish.mvc.tools.seed;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

// Chuyển tiêu đề tiếng Việt có dấu thành tên file an toàn (bỏ dấu, thay khoảng trắng bằng "-").
final class SlugUtil {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private SlugUtil() {
    }

    static String slugify(String input) {
        String noAccent = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                // Normalizer không tách đ/Đ (không phải tổ hợp base+dấu) -> thay thủ công.
                .replace('đ', 'd').replace('Đ', 'D');
        String lower = noAccent.toLowerCase(Locale.ROOT);
        String slug = NON_ALNUM.matcher(lower).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "tai-lieu" : slug;
    }
}
