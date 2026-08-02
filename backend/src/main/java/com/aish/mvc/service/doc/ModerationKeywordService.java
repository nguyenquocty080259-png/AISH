package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.ModerationKeywordCreateRequestDTO;
import com.aish.mvc.dto.doc.ModerationKeywordResponseDTO;
import com.aish.mvc.dto.doc.ModerationKeywordUpdateRequestDTO;
import com.aish.mvc.entity.enums.ModerationKeywordType;

import java.util.List;

/**
 * Quản lý danh sách TỪ KHOÁ CẤM cho trang quản trị. Từ khoá chia theo loại: NAMING (đặt tên),
 * COMMENT (bình luận), DOCUMENT_CONTENT (nội dung tài liệu). Lưu trong database nên Admin sửa
 * là có hiệu lực ngay, không cần sửa code hay khởi động lại server.
 */
public interface ModerationKeywordService {

    /** Liệt kê từ khoá, mới nhất trước. type = null thì lấy tất cả các loại. */
    List<ModerationKeywordResponseDTO> list(ModerationKeywordType type);

    /** Thêm từ khoá mới (mặc định đang bật). Trùng từ trong cùng một loại thì báo lỗi 409. */
    ModerationKeywordResponseDTO create(ModerationKeywordCreateRequestDTO request);

    /** Sửa chữ và/hoặc bật-tắt từ khoá. Tắt thì từ khoá còn trong danh sách nhưng thôi chặn. */
    ModerationKeywordResponseDTO update(Long id, ModerationKeywordUpdateRequestDTO request);

    /** Xoá hẳn từ khoá khỏi danh sách. */
    void delete(Long id);
}
