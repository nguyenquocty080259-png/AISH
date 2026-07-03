package com.aish.mvc.service.doc;

import com.aish.mvc.entity.doc.Subject;
import java.util.List;

public interface SubjectService {
    List<Subject> getAllSubjects();
    Subject findOrCreate(Subject subject);
    Subject updateSubject(Long id, String name, String description);
    void deleteSubject(Long id);
}
