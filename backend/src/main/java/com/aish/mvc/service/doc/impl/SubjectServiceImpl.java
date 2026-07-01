package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.repository.doc.SubjectRepository;
import com.aish.mvc.service.doc.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Override
    public Subject findOrCreate(Subject subject) {
        return subjectRepository.findByName(subject.getName())
                .orElseGet(() -> subjectRepository.save(subject));
    }
}
