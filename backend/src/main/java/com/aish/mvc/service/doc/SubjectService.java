package com.aish.mvc.service.doc;

import com.aish.mvc.entity.doc.Subject;
import java.util.List;

/**
 * Danh mục MÔN HỌC dùng để phân loại tài liệu (Toán, Lý, Lập trình...). Mỗi tài liệu bắt buộc
 * thuộc ít nhất một môn; người dùng lọc tài liệu theo môn ở trang Cộng đồng.
 * Xem thì ai cũng được, thêm/sửa/xoá chỉ Admin (chốt quyền ở tầng controller).
 */
public interface SubjectService {
    /** Toàn bộ môn học — đổ vào ô chọn môn ở form upload và bộ lọc trang Cộng đồng. */
    List<Subject> getAllSubjects();

    /** Tìm môn theo tên, chưa có thì tạo mới — thêm môn đã tồn tại cũng không sinh bản trùng. */
    Subject findOrCreate(Subject subject);

    /** Sửa tên/mô tả môn học. Tên không được rỗng và không được trùng môn khác. */
    Subject updateSubject(Long id, String name, String description);

    /** Xoá môn học. Bị CHẶN nếu còn tài liệu đang gán môn này, tránh tài liệu "mồ côi" 0 môn. */
    void deleteSubject(Long id);
}
