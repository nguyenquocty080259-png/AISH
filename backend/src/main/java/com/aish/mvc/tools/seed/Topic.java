package com.aish.mvc.tools.seed;

import java.util.List;

/**
 * Một "bài học" hoàn chỉnh — nội dung học thuật thật, sẽ được render thành 1 (hoặc nhiều,
 * nếu có variant định dạng) file trên đĩa. level = "THPT" | "UNIVERSITY". track chỉ có ở
 * UNIVERSITY ("IT" | "IB" | "MKT") — null với THPT vì subjectOrDomain đã đủ rõ nghĩa.
 */
public record Topic(
        String level,
        String track,
        String subjectOrDomain,
        String title,
        List<String> paragraphs
) {
}
