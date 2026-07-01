package com.aish.mvc.dto.doc;

import org.springframework.core.io.Resource;

public record DocumentDownloadResult(Resource resource, String fileName) {}
