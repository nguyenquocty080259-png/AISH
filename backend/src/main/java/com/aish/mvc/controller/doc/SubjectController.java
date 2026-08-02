package com.aish.mvc.controller.doc;

import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.service.doc.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * API cho danh mục MÔN HỌC (/api/subjects) — dữ liệu để phân loại và lọc tài liệu.
 *
 * <p>Phân quyền đặt ngay trên từng method bằng @PreAuthorize: XEM thì user thường cũng được (vì
 * form upload và bộ lọc trang Cộng đồng đều cần), còn THÊM/SỬA/XOÁ chỉ Admin.
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // Danh sách môn học — user thường và Admin đều gọi được.
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<List<Subject>> getAll() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    // Thêm môn học mới — chỉ Admin. Môn đã tồn tại thì trả về môn cũ, không tạo bản trùng.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Subject> create(@RequestBody Subject request) {
        return ResponseEntity.ok(subjectService.findOrCreate(request));
    }

    // Sửa tên/mô tả môn học — chỉ Admin.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Subject> update(@PathVariable Long id, @RequestBody Subject request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request.getName(), request.getDescription()));
    }

    // Xoá môn học — chỉ Admin, và bị chặn nếu còn tài liệu đang gán môn này.
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}