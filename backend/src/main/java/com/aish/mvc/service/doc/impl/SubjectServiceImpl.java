package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.SubjectRepository;
import com.aish.mvc.service.doc.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * QUẢN LÝ MÔN HỌC — danh mục dùng để phân loại tài liệu (Toán, Lý, Lập trình...). Mỗi tài liệu
 * bắt buộc thuộc ít nhất một môn, và người dùng lọc tài liệu theo môn ở trang Cộng đồng.
 *
 * <p>Xem danh sách thì user nào cũng được; thêm/sửa/xoá chỉ Admin (chốt ở tầng controller).
 */
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final DocDocumentRepository docDocumentRepository;

    /** Toàn bộ môn học — dùng để đổ vào ô chọn môn ở form upload và bộ lọc trang Cộng đồng. */
    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    /**
     * Tìm môn theo tên, chưa có thì tạo mới. Đầu vào: môn học (chỉ cần tên). Trả về: môn học
     * trong database. Nhờ vậy Admin thêm môn đã tồn tại cũng không tạo ra bản trùng.
     */
    @Override
    public Subject findOrCreate(Subject subject) {
        return subjectRepository.findByName(subject.getName())
                .orElseGet(() -> subjectRepository.save(subject));
    }

    /**
     * SỬA môn học. Đầu vào: id, tên mới, mô tả mới. Trả về: môn học sau khi sửa.
     * Các bước: (1) tìm môn, (2) tên không được rỗng, (3) không được trùng tên môn khác, (4) lưu.
     */
    @Override
    @Transactional
    public Subject updateSubject(Long id, String name, String description) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Môn học không tồn tại!"));

        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.subject.nameRequired");
        }
        subjectRepository.findByName(cleaned)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "error.subject.duplicate");
                });

        subject.setName(cleaned);
        subject.setDescription(description);
        return subjectRepository.save(subject);
    }

    /**
     * XOÁ môn học. Đầu vào: id. CHẶN nếu còn tài liệu nào đang gán môn này — vì mỗi tài liệu
     * bắt buộc thuộc ít nhất một môn, xoá bừa sẽ để lại tài liệu "mồ côi" không thuộc môn nào.
     */
    @Override
    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Môn học không tồn tại!"));

        // DEC-030: mỗi document phải thuộc >=1 môn -> KHÔNG cho xóa subject nếu còn document
        // nào tham chiếu (kể cả chỉ 1 document đang gán đúng 1 subject này), để không tạo ra
        // document mồ côi 0 subject.
        if (docDocumentRepository.existsBySubjects_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể xóa môn học đang được gán cho tài liệu.");
        }

        subjectRepository.delete(subject);
    }
}
