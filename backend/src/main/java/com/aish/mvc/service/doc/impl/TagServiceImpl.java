package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.doc.Tag;
import com.aish.mvc.repository.doc.TagRepository;
import com.aish.mvc.service.doc.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}
