package com.aish.mvc.service.doc;

import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.service.ai.ToxicKeywordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * KIỂM DUYỆT TÊN do người dùng đặt — dùng cho tiêu đề tài liệu và tên bộ sưu tập.
 *
 * <p>Chặn hai nhóm: tên vô nghĩa (quá ngắn, toàn số, một ký tự lặp lại như "aaaa") và tên chứa
 * từ ngữ không phù hợp. Mục đích là giữ danh mục tài liệu sạch và tìm kiếm được.
 */
@Service
@RequiredArgsConstructor
public class NamingModerationService {

    private final ToxicKeywordFilter toxicKeywordFilter;

    /**
     * Kiểm tra một cái tên. Đầu vào: tên người dùng nhập. Trả về: tên đã cắt khoảng trắng thừa
     * (dùng luôn giá trị này để lưu). Không hợp lệ thì ném lỗi 400.
     */
    public String validate(String value) {
        String cleaned = value == null ? "" : value.trim();
        // Chặn tên vô nghĩa: dưới 3 ký tự, toàn chữ số ("12345"), hoặc lặp một ký tự ("aaaaa").
        if (cleaned.length() < 3
                || cleaned.chars().allMatch(Character::isDigit)
                || isSingleRepeatedCharacter(cleaned)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.naming.invalid");
        }
        // Chặn tên chứa từ cấm loại NAMING (danh sách Admin quản lý trong database).
        if (toxicKeywordFilter.matches(cleaned, ModerationKeywordType.NAMING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.naming.badWord");
        }
        return cleaned;
    }

    private boolean isSingleRepeatedCharacter(String value) {
        if (value.isEmpty()) return false;
        int first = value.codePointAt(0);
        return value.codePoints().allMatch(codePoint -> codePoint == first);
    }
}
