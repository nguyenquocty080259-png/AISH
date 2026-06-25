package com.aish.mvc.controller.doc;

import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.repository.doc.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository subjectRepository;

    // Lấy danh sách môn để FE đổ vào dropdown
    @GetMapping
    public ResponseEntity<List<Subject>> getAll() {
        return ResponseEntity.ok(subjectRepository.findAll());
    }

    // Tạo môn mới (khi user gõ môn chưa có)
    @PostMapping
    public ResponseEntity<Subject> create(@RequestBody Subject request) {
        Subject existing = subjectRepository.findByName(request.getName()).orElse(null);
        if (existing != null) return ResponseEntity.ok(existing); // đã có thì trả về luôn
        return ResponseEntity.ok(subjectRepository.save(request));
    }
}