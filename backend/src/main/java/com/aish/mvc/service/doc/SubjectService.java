package com.aish.mvc.service.doc;

import com.aish.mvc.entity.doc.Subject;
import java.util.List;

public interface SubjectService {
    List<Subject> getAllSubjects();
    Subject findOrCreate(Subject subject);
}
