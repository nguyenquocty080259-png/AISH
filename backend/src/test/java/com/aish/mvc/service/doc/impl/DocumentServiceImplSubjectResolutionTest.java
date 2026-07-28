package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.repository.doc.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * resolveSubjects() dùng chung cho lúc tạo và lúc sửa tài liệu. Điểm quan trọng: id môn học
 * không tồn tại phải làm CẢ thao tác thất bại, không được âm thầm bỏ qua — trước đây
 * findById().ifPresent() nuốt id sai nên tài liệu được lưu với ít môn hơn người dùng đã chọn
 * mà không báo gì. Package-private để test gọi thẳng, không phải dựng lại pipeline upload.
 */
class DocumentServiceImplSubjectResolutionTest {

    private final SubjectRepository subjectRepository = mock(SubjectRepository.class);

    private DocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "subjectRepository", subjectRepository);
    }

    private void givenExistingSubjects(Long... ids) {
        when(subjectRepository.findById(anyLong())).thenReturn(Optional.empty());
        for (Long id : ids) {
            Subject subject = new Subject();
            subject.setId(id);
            subject.setName("Môn " + id);
            when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));
        }
    }

    @Test
    void resolvesEveryExistingSubject() {
        givenExistingSubjects(1L, 2L);

        Set<Subject> subjects = service.resolveSubjects(List.of(1L, 2L));

        assertEquals(2, subjects.size());
    }

    @Test
    void rejectsListContainingUnknownSubjectId() {
        givenExistingSubjects(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.resolveSubjects(List.of(1L, 999L)));

        // Báo rõ id nào sai để FE hiển thị được, thay vì lặng lẽ lưu thiếu môn.
        assertTrue(exception.getMessage().contains("999"), exception.getMessage());
    }

    @Test
    void rejectsListWhereEverySubjectIdIsUnknown() {
        givenExistingSubjects();

        assertThrows(IllegalArgumentException.class, () -> service.resolveSubjects(List.of(7L, 8L)));
    }

    @Test
    void ignoresNullEntriesButStillRequiresAtLeastOneSubject() {
        givenExistingSubjects();

        assertThrows(IllegalArgumentException.class,
                () -> service.resolveSubjects(Arrays.asList((Long) null, null)));
    }
}
