package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.doc.AdminAppealResponseDTO;
import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.dto.doc.AppealDecisionRequestDTO;
import com.aish.mvc.entity.enums.AppealStatus;
import com.aish.mvc.service.admin.AdminService;
import com.aish.mvc.service.doc.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Mọi route ở đây nằm dưới /api/admin/** -> SecurityConfig đã gate .hasRole("ADMIN").
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DocumentService documentService;

    @GetMapping("/appeals")
    public ResponseEntity<List<AdminAppealResponseDTO>> listAppeals(
            @RequestParam(value = "status", required = false) AppealStatus status) {
        return ResponseEntity.ok(adminService.listAppeals(status));
    }

    @PostMapping("/appeals/{appealId}/approve")
    public ResponseEntity<AdminAppealResponseDTO> approveAppeal(
            @PathVariable Long appealId,
            @RequestBody(required = false) AppealDecisionRequestDTO body) {
        String note = body != null ? body.getNote() : null;
        return ResponseEntity.ok(adminService.approveAppeal(appealId, note));
    }

    @PostMapping("/appeals/{appealId}/reject")
    public ResponseEntity<AdminAppealResponseDTO> rejectAppeal(
            @PathVariable Long appealId,
            @RequestBody(required = false) AppealDecisionRequestDTO body) {
        String note = body != null ? body.getNote() : null;
        return ResponseEntity.ok(adminService.rejectAppeal(appealId, note));
    }

    @GetMapping("/documents")
    public ResponseEntity<Page<AdminDocumentSummaryDTO>> listDocuments(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(documentService.getAllDocumentsForAdmin(pageable));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.adminDeleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
