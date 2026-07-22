package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.CommunityPageResponseDTO;
import com.aish.mvc.service.doc.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentSearchController {

    private final DocumentService documentService;

    // Trang Cộng đồng: chỉ tài liệu PUBLIC, tìm kiếm + lọc môn + sắp xếp + phân trang.
    // sortBy: "newest" (mặc định) | "downloads" | "rating"
    @GetMapping("/community")
    public ResponseEntity<CommunityPageResponseDTO> getCommunity(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "12") int size) {
        return ResponseEntity.ok(documentService.getCommunityDocuments(keyword, subjectId, minRating, sortBy, page, size));
    }
}