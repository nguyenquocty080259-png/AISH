package com.aish.mvc.controller.doc;

import com.aish.mvc.entity.doc.Tag;
import com.aish.mvc.service.doc.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<Tag>> getAll() {
        return ResponseEntity.ok(tagService.getAllTags());
    }
}