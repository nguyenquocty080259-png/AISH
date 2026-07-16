package com.aish.mvc.dto.ai;

import java.util.List;

public record MetadataSuggestionDTO(String title, String description, List<Long> subjectIds) {}
