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

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final DocDocumentRepository docDocumentRepository;

    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Override
    public Subject findOrCreate(Subject subject) {
        return subjectRepository.findByName(subject.getName())
                .orElseGet(() -> subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public Subject updateSubject(Long id, String name, String description) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Môn học không tồn tại!"));

        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên môn học không được để trống!");
        }
        subjectRepository.findByName(cleaned)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Đã tồn tại môn học với tên này!");
                });

        subject.setName(cleaned);
        subject.setDescription(description);
        return subjectRepository.save(subject);
    }

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
