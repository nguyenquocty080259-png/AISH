package com.aish.mvc.controller.doc;

import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.service.doc.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<List<Subject>> getAll() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @PostMapping
    public ResponseEntity<Subject> create(@RequestBody Subject request) {
        return ResponseEntity.ok(subjectService.findOrCreate(request));
    }
}